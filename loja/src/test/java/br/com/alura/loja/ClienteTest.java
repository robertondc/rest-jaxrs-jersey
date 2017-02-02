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

	@Before
	public void iniciaServidor(){
		server = Servidor.iniciaServidor();
	}
	
	@Before
	public void iniciaCliente(){
		Client client = ClientBuilder.newClient();
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
		String xml = carrinho.getXml();
		
		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML); //representa oque será enviado
		Response response = target.path("/carrinhos").request().post(entity);
		Assert.assertEquals("<status>sucesso</status>", response.readEntity(String.class));
	}
	
	@Test
	public void testaQueAInsercaoDeProjetosFunciona(){
		Projeto projeto = new Projeto(1l, "novo projeto", 2015);
		String xml = projeto.toXml();
		
		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);
		Response response = target.path("/projetos").request().post(entity);
		Assert.assertEquals("<status>sucessoProjeto</status>", response.readEntity(String.class));
		
	}
	
}
