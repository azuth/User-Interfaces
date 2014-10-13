package de.hska.iwii.picturecommunity.backend.rest;

import java.io.Serializable;

public class UploadedFile implements Serializable {

	private static final long serialVersionUID = -38331060124340967L;
	private String name;
	private Integer size;
	private String url;
	private String thumbnailUrl;
	private String deleteUrl;
	private String deleteType;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDeleteUrl() {
		return deleteUrl;
	}

	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}

	public String getDeleteType() {
		return deleteType;
	}

	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}

	public UploadedFile() {
		super();
	}
	
	public UploadedFile(String name, Integer size, String url) {
		super();
		this.name = name;
		this.size = size;
		this.url = url;
	}
	
	public UploadedFile(String name, Integer size, String url,
			String thumbnail_url, String delete_url, String delete_type) {
		super();
		this.name = name;
		this.size = size;
		this.url = url;
		this.thumbnailUrl = thumbnail_url;
		this.deleteUrl = delete_url;
		this.deleteType = delete_type;
	}
	
	
}
