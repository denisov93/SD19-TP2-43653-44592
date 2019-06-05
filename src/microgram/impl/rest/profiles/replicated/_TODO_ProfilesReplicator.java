package microgram.impl.rest.profiles.replicated;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.GetProfile;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.rest.replication.MicrogramOperation;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;

public abstract class _TODO_ProfilesReplicator implements MicrogramOperationExecutor, Profiles {

	private static final int FOLLOWER = 0, FOLLOWEE = 1;
	
	final Profiles localReplicaDB;
	final OrderedExecutor executor;
	
	_TODO_ProfilesReplicator( Profiles localDB, OrderedExecutor executor) {
		this.localReplicaDB = localDB;
		this.executor = executor.init(this);
	}
	
	@Override
	public Result<?> execute( MicrogramOperation op ) {
		switch( op.type ) {
			case CreateProfile: {
				return localReplicaDB.createProfile( op.arg( Profile.class));
			}
			case IsFollowing: {
				String[] users = op.args(String[].class);
				return localReplicaDB.isFollowing( users[FOLLOWER], users[FOLLOWEE]);
			}
			default:
				return error(NOT_IMPLEMENTED);
		}	
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return executor.replicate( new MicrogramOperation(GetProfile, userId));
	}
}
