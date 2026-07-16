package com.sleekydz86.catalogflow.global.exception;

public class StorageObjectNotFoundException extends ApplicationException {

	public StorageObjectNotFoundException(String storageKey) {
		super("저장소에서 객체를 찾을 수 없습니다: " + storageKey);
	}
}
