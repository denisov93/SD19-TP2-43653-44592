package microgram.impl.rest.profiles.replicated;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.rest.RestProfiles;
import microgram.impl.java.JavaProfiles;
import microgram.impl.rest.RestResource;

public class ReplicatedProfilesResources extends RestResource implements RestProfiles {
	final Profiles localDB;
	final _TODO_ProfilesReplicator replicator;
	
	public ReplicatedProfilesResources() {
		this.localDB = new JavaProfiles() ;
		this.replicator = null; //new _TODO_ProfilesReplicator(localDB, new TotalOrderExecutor(MicrogramTopic.MicrogramEvents));
	}

	@Override
	public Profile getProfile(String userId) {
		return super.resultOrThrow( replicator.getProfile( userId ));
	}

	@Override
	public void createProfile(Profile profile) {
		super.resultOrThrow( replicator.createProfile(profile));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		super.resultOrThrow( replicator.follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		return super.resultOrThrow( replicator.isFollowing(userId1, userId2));
	}

	@Override
	public void deleteProfile(String userId) {
		super.resultOrThrow( replicator.deleteProfile(userId));
	}

	@Override
	public List<Profile> search(String prefix) {
		return super.resultOrThrow( replicator.search(prefix));
	}
}
