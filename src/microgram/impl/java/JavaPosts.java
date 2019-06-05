package microgram.impl.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.impl.java.JavaProfiles.Profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;

public final class JavaPosts implements Posts {

	static JavaPosts Posts;
	
	private static final Set<String> EMPTY_SET = new HashSet<>();
	
	final Map<String, Post> posts = new ConcurrentHashMap<>();
	final Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	final Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	public JavaPosts() {
		Posts = this;
	}

	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.get(postId);
		if (res != null) {
			res.setLikes( likes.getOrDefault(postId, Collections.emptySet()).size());
			return ok(res);
		}
		else
			return error(NOT_FOUND);
	}
	
	@Override
	public Result<String> createPost(Post post) {
		String ownerId = post.getOwnerId();
		
		if( ! Profiles.getProfile( ownerId ).isOK() )
			return error(NOT_FOUND);
		
		String postId = post.getPostId();

		posts.putIfAbsent(postId, post);
		likes.putIfAbsent(postId, ConcurrentHashMap.newKeySet());
		userPosts.computeIfAbsent(ownerId, (__) -> ConcurrentHashMap.newKeySet()).add( postId );
		
		return ok(postId);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Post post = posts.remove(postId);
		if (post != null) {
			likes.remove(postId);
			userPosts.getOrDefault(post.getOwnerId(), EMPTY_SET).remove(postId);
			return ok();
		} else
			return error(NOT_FOUND);
	}
	
	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Set<String> res = likes.get(postId);
		if (res == null)
			return error(NOT_FOUND);

		if (isLiked) {
			if (!res.add(userId))
				return error(CONFLICT);
		} else {
			if (!res.remove(userId))
				return error(NOT_FOUND);
		}
		return ok();
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Set<String> res = likes.get(postId);
		if (res != null)
			return ok(res.contains(userId));
		else
			return error(NOT_FOUND);
	}
	
	@Override
	public Result<List<String>> getPosts(String userId) {
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return error(NOT_FOUND);
	}


	@Override
	public Result<List<String>> getFeed(String userId) {
		Set<String> following = Profiles.following(userId );
		if( following != null ) {
			List<String> feed = new ArrayList<>();
			for( String followee : following )
				feed.addAll( userPosts.getOrDefault( followee, EMPTY_SET ));
			return ok( feed );
		}
		else
			return error(NOT_FOUND);
	}

	int getUserPostsStats( String userId) {
		return userPosts.getOrDefault(userId, EMPTY_SET).size();
	}
	
	void deleteAllUserPosts(String userId) {
		for( String postId : userPosts.getOrDefault(userId, EMPTY_SET))
			deletePost( postId );
	}

}
