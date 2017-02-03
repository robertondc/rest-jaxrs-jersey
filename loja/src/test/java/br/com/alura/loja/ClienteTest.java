package br.com.alura.loja;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
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
		client = ClientBuilder.newClient();
		target = client.target("http://localhost:8080");
	}
	
	@After
	public void paraServidor(){
		server.stop();
	}
	
	@Test
	public void testaQueAConexaoComOServidorFunciona() {
		String conteudo = target.path("/carrinhos/1").request().get(String.class);
		Carrinho carrinho = (Carrinho) new XStream().fromXML(conteudo);
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
		carrinho.adiciona(new Produto(314L, "Tablet", 999, 1));
		carrinho.setRua("Rua Vergueiro");
		carrinho.setCidade("Sao Paulo");
		String xml = carrinho.toXml();
		
		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML); //representa oque será enviado
		Response response = target.path("/carrinhos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		
		String location = response.getHeaderString("Location");
		String conteudo = client.target(location).request().get(String.class);
		Assert.assertTrue(conteudo.contains("Tablet"));
		
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
		carrinho.adiciona(new Produto(456l, "queijo", 888, 2));
		carrinho.setRua("Rua das batatas, 123");
		carrinho.setCidade("Sao Paulo");
		String xml = carrinho.toXml();
		
		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);
		Response responsePost = (Response) target.path("/carrinhos").request().post(entity);
		String locationProduto = responsePost.getHeaderString("Location");
		
		Assert.assertEquals(201, responsePost.getStatus());
			
		String xmlPreDelete = client.target(locationProduto).request().get(String.class);
		Carrinho carrinhoRetornoInsert = (Carrinho) new XStream().fromXML(xmlPreDelete);
		
		Response responseDelete = target.path("/carrinhos/" + carrinhoRetornoInsert.getId() + "/produtos/" + produtoRemover.getId()).request().delete();
		
		Assert.assertEquals(200, responseDelete.getStatus());
		
		String xmlPosDelete = client.target(locationProduto).request().get(String.class);
		Carrinho carrinhoRetornoDelete = (Carrinho) new XStream().fromXML(xmlPosDelete);

		Assert.assertTrue(carrinhoRetornoDelete.getProdutos().size() == 1);
		
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
	
	
}
