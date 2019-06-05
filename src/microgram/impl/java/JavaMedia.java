package microgram.impl.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.io.File;
import java.nio.file.Files;

import microgram.api.java.Media;
import microgram.api.java.Result;
import utils.Hash;

public class JavaMedia implements Media {

	private static final String MEDIA_EXTENSION = ".jpg";
	private static final String ROOT_DIR = "/tmp/microgram/";

	public JavaMedia() {
		new File(ROOT_DIR).mkdirs();
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		try {
			String id = Hash.of(bytes);
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);

			if (filename.exists())
				return error(CONFLICT);

			Files.write(filename.toPath(), bytes);
			return Result.ok(id);
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}

	@Override
	public Result<byte[]> download(String id) {
		try {
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
			if (filename.exists())
				return ok(Files.readAllBytes(filename.toPath()));
			else
				return error(NOT_FOUND);
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> delete(String id) {
		try {
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
			if (Files.deleteIfExists(filename.toPath()))
				return ok();
			else
				return error(NOT_FOUND);
		} catch (Exception x) {
			return error(INTERNAL_ERROR);
		}
	}
}
