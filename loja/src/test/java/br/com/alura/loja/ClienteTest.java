package br.com.alura.loja;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import br.com.alura.loja.modelo.Carrinho;
import br.com.alura.loja.modelo.Produto;
import br.com.alura.loja.modelo.Projeto;

public class ClienteTest {

	private HttpServer server;
	private WebTarget target;
	private Client client;

	@Before
	public void iniciaServidor(){
		server = Servidor.iniciaServidor();
	}
	
	@Before
	public void iniciaCliente(){
		ClientConfig config = new ClientConfig();
		config.register(new LoggingFilter());
		client = ClientBuilder.newClient(config);
		target = client.target("http://localhost:8080");
	}
	
	@After
	public void paraServidor(){
		server.stop();
	}
	
	@Test
	public void testaQueAConexaoComOServidorFunciona() {
		Carrinho carrinho = target.path("/carrinhos/1").request().get(Carrinho.class);
		Assert.assertEquals("Rua Vergueiro 3185, 8 andar", carrinho.getRua());
	}

	@Test
	public void testaQueAConexaFuncionaComOServidorNoPathDeProjetos(){
		String conteudo = target.path("/projetos/1").request().get(String.class);
		Projeto projeto = (Projeto) new XStream().fromXML(conteudo);
		Assert.assertEquals("Minha Loja", projeto.getNome());
	}
	
	@Test
	public void testaQueAInsercaoDeCarrinhosFunciona(){
		Carrinho carrinho = new Carrinho();
		String nomeProduto = "Tablet";
		carrinho.adiciona(new Produto(314L, nomeProduto, 999, 1));
		carrinho.setRua("Rua Vergueiro");
		carrinho.setCidade("Sao Paulo");
		
		Entity<Carrinho> entity = Entity.entity(carrinho, MediaType.APPLICATION_XML); //representa oque será enviado
		Response response = target.path("/carrinhos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		
		String location = response.getHeaderString("Location");
		Carrinho retorno = client.target(location).request().get(Carrinho.class);
		Assert.assertEquals(nomeProduto,retorno.getProduto(314).getNome());
		
	}
	
	@Test
	public void testaQueAInsercaoDeProjetosFunciona(){
		Projeto projeto = new Projeto(1l, "novo projeto", 2015);
		String xml = projeto.toXml();
		
		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);
		Response response = target.path("/projetos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		
		String location = response.getHeaderString("Location");
		String conteudo = client.target(location).request().get(String.class);
		Assert.assertTrue(conteudo.contains("novo projeto"));
		
	}
	
	@Test
	public void testaQueARemocaoDoCarrinhoEstaFuncionando(){
		Carrinho carrinho = new Carrinho();
		Produto produtoRemover = new Produto(123l, "batata", 999, 1);
		carrinho.adiciona(produtoRemover);
		Produto produtoNaoRemover = new Produto(456l, "queijo", 888, 2);
		carrinho.adiciona(produtoNaoRemover);
		carrinho.setRua("Rua das batatas, 123");
		carrinho.setCidade("Sao Paulo");
		
		Entity<Carrinho> entity = Entity.entity(carrinho, MediaType.APPLICATION_XML);
		Response post = (Response) target.path("/carrinhos").request().post(entity);
		String locationCarrinho = post.getHeaderString("Location");
		
		Assert.assertEquals(201, post.getStatus());
			 
		Carrinho carrinhoInserido = (Carrinho) client.target(locationCarrinho).request().get(Carrinho.class);
		
		Response delete = target.path("/carrinhos/" + carrinhoInserido.getId() + "/produtos/" + produtoRemover.getId()).request().delete();
		
		Assert.assertEquals(200, delete.getStatus());
		
		Carrinho carrinhoComProdutoDeletado = (Carrinho) client.target(locationCarrinho).request().get(Carrinho.class);

		Assert.assertTrue(carrinhoComProdutoDeletado.getProdutos().size() == 1);
		Assert.assertNull(carrinhoComProdutoDeletado.getProduto(produtoRemover.getId()));
		Assert.assertNotNull(carrinhoComProdutoDeletado.getProduto(produtoNaoRemover.getId()));
		
	}
	
	@Test
	public void testaQueARemocaoDeProjetoEstaFuncionando(){
		Projeto projeto = new Projeto(1l, "Queijo", 2016);
		String xmlInsert = projeto.toXml();
		
		Entity<String> entity = Entity.entity(xmlInsert, MediaType.APPLICATION_XML);
		Response responsePost = target.path("/projetos").request().post(entity);
		
		String locationProjeto = responsePost.getHeaderString("Location");
		
		Assert.assertEquals(201,  responsePost.getStatus());
		
		String xmlRetorno = client.target(locationProjeto).request().get(String.class);
		Projeto projetoRetornoInsert = (Projeto) new XStream().fromXML(xmlRetorno);
		
		Response responseDelete = target.path("/projetos/" + projetoRetornoInsert.getId()).request().delete();
		Assert.assertEquals(200, responseDelete.getStatus());
		
		Response responsePosDelete = target.path("/projetos/" + projetoRetornoInsert.getId()).request().get();
		Assert.assertEquals(404, responsePosDelete.getStatus());
	}
	

	@Test
	public void testaQueAAtualizacaoDeProdutosEstaFuncionando(){
		Carrinho carrinho = new Carrinho();
		
		Produto produtoAlterar = new Produto(555l, "batata", 60.5, 2);
		carrinho.adiciona(produtoAlterar);
		carrinho.adiciona(new Produto(666l, "queijo", 30.1, 2));
		carrinho.setRua("Rua das batatas, 123");
		carrinho.setCidade("Sao Paulo");
		
		Entity<Carrinho> entity = Entity.entity(carrinho, MediaType.APPLICATION_XML);
		Response post = (Response) target.path("/carrinhos").request().post(entity);
		String locationCarrinho = post.getHeaderString("Location");
		
		Assert.assertEquals(201, post.getStatus());
		
		Carrinho carrinhoAlterar = client.target(locationCarrinho).request().get(Carrinho.class);
		
		produtoAlterar.setQuantidade(1);
	
		Entity<Produto> entityProduto = Entity.entity(produtoAlterar, MediaType.APPLICATION_XML);
		String uriAlterarQuantidade = "/carrinhos/" + carrinhoAlterar.getId() + "/produtos/" + produtoAlterar.getId() + "/quantidade";
		Response put = target.path(uriAlterarQuantidade).request().put(entityProduto);
		
		Assert.assertEquals(200, put.getStatus());
		
		Carrinho carrinhoAlterado = (Carrinho) target.path("/carrinhos/" + carrinhoAlterar.getId()).request().get(Carrinho.class);
		
		Produto produtoAlterado = carrinhoAlterado.getProduto(produtoAlterar.getId());
		Assert.assertEquals(1, produtoAlterado.getQuantidade());
		

	}
	
}
