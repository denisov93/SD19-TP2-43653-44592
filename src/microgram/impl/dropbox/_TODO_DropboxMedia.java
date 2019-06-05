package microgram.impl.dropbox;

import dropbox.DropboxClient;
import microgram.api.java.Media;
import microgram.api.java.Result;
import utils.Hash;

public class _TODO_DropboxMedia implements Media {
	
	private static DropboxClient dc;
	private static final String ROOT = "/tmp/microgram/";
	
	public _TODO_DropboxMedia() throws Exception {
		dc = DropboxClient.createClientWithAccessToken();
		dc.createDirectory(ROOT);
	}
	
	@Override
	public Result<String> upload(byte[] bytes) {
		String as;
		try {	
			as = Hash.of(bytes);
			dc.upload(as, bytes);
			return Result.ok(as);
		}catch(Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
	
	@Override
	public Result<byte[]> download(String id) {
		try {
			return dc.download(id);
		}catch(Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> delete(String id) {
		try {
			return dc.delete(id);
		}catch(Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
	
	
}
