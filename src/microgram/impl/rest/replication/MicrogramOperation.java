package microgram.impl.rest.replication;

import utils.JSON;
import utils.Random;

public class MicrogramOperation {
	
	public static enum Operation {
		CreatePost, GetPost, DeletePost, LikePost, UnLikePost, IsLiked, GetPosts, GetFeed, CreateProfile, GetProfile, DeleteProfile, FollowProfile, UnFollowProfile, SearchProfile, IsFollowing 
	}
	
	private static final String DELIMITER = "\t";
	public final String id;
	public final Operation type;
	private final String jsonArgs;
		
	public MicrogramOperation(Operation type, Object args) {
		this.type = type;
		this.id = Random.key128();
		this.jsonArgs = JSON.encode( args);
	}

	public MicrogramOperation( String encoding ) {
		String[] tokens = encoding.split(DELIMITER);
		this.id = tokens[0];
		this.type = Operation.valueOf(tokens[1]);
		this.jsonArgs = tokens[2];
	}

	public String encode() {
		return new StringBuilder(id)
				.append(DELIMITER)
				.append(type.name())
				.append(DELIMITER)
				.append(jsonArgs).toString();
	}

	public <T> T arg(Class<T> classOf) {
		return JSON.decode( jsonArgs, classOf);
	}
	
	public <T> T args(Class<T> classOf) {
		return JSON.decode( jsonArgs, classOf);
	}
}