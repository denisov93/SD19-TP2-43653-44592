package microgram.impl.rest.profiles;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.rest.RestProfiles;
import microgram.impl.rest.RestResource;

public class RestProfilesResources extends RestResource implements RestProfiles {

	protected final Profiles impl;

	public RestProfilesResources(Profiles impl) {
		this.impl = impl;
	}

	@Override
	public Profile getProfile(String userId) {
		return super.resultOrThrow(impl.getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) {
		super.resultOrThrow(impl.createProfile(profile));
	}

	@Override
	public List<Profile> search(String prefix) {
		return super.resultOrThrow(impl.search(prefix));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		super.resultOrThrow(impl.follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		return super.resultOrThrow(impl.isFollowing(userId1, userId2));
	}

	@Override
	public void deleteProfile(String userId) {
		super.resultOrThrow(impl.deleteProfile(userId));
	}
}
