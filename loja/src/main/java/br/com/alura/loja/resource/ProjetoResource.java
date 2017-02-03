package br.com.alura.loja.resource;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.thoughtworks.xstream.XStream;

import br.com.alura.loja.dao.ProjetoDao;
import br.com.alura.loja.modelo.Projeto;

@Path("projetos")
public class ProjetoResource {
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response busca(@PathParam("id") long id){
		Projeto projeto = new ProjetoDao().busca(id);
		if (projeto == null) {
			return Response.status(404).build();
		} else { 
			return Response.ok(projeto.toXml()).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response adiciona(String conteudo){
		Projeto projeto = (Projeto) new XStream().fromXML(conteudo);
		new ProjetoDao().adiciona(projeto);
		URI uri = URI.create("/projetos/" + projeto.getId());
		return Response.created(uri).build();
	}
	
	@DELETE
	@Path("{id}")
	public Response remove(@PathParam("id") long id){
		new ProjetoDao().remove(id);
		return Response.ok().build();
	}
}
