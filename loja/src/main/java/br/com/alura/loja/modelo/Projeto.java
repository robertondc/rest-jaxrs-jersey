package br.com.alura.loja.modelo;

import com.thoughtworks.xstream.XStream;

public class Projeto {

	private Long id;
	private String nome;
	private Integer anoDeInicio;

	public Projeto(Long id, String nome, Integer anoDeInicio) {
		this.id = id;
		this.nome = nome;
		this.anoDeInicio = anoDeInicio;
	}

	public Projeto() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public Integer getAnoDeInicio() {
		return anoDeInicio;
	}

	public String toXml() {
		return new XStream().toXML(this);
	}

}
