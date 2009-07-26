package org.jogre.server.data.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jogre.server.data.ProtoSchema;
import org.jogre.server.data.ProtoSchema.GameInfo;
import org.jogre.server.data.ProtoSchema.GameSummary;
import org.jogre.server.data.ProtoSchema.Snapshot;
import org.jogre.server.data.ProtoSchema.User;

import com.appenginefan.toolkit.persistence.MapBasedPersistence;
import com.appenginefan.toolkit.persistence.Persistence;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import static org.jogre.server.data.db.IDatabase.*;

/**
 * ORM implementation that maps to Persistence objects
 * 
 * @author Jens Scheffler
 *
 */
public class PersistenceORM implements ORM {
  
  private static final Logger LOG = Logger.getLogger(PersistenceORM.class.getName());
  
  private static void checkId(String id, String... supportedIds) {
    for (String test : supportedIds) {
      if (test.equals(id)) {
        return;
      }
    }
    throw new UnsupportedOperationException(id);
  }
  
  private Persistence<ProtoSchema.GameInfo> infos;
  private Persistence<ProtoSchema.GameSummary> summaries;
  private Persistence<ProtoSchema.Snapshot> snapshots;
  private Persistence<ProtoSchema.User> users;

  public PersistenceORM(Persistence<GameInfo> infos,
      Persistence<GameSummary> summaries,
      Persistence<Snapshot> snapshots,
      Persistence<User> users) {
    Preconditions.checkNotNull(infos);
    Preconditions.checkNotNull(summaries);
    Preconditions.checkNotNull(snapshots);
    Preconditions.checkNotNull(users);
    this.infos = infos;
    this.summaries = summaries;
    this.snapshots = snapshots;
    this.users = users;
  }
  
  public static PersistenceORM createInMemoryInstance() {
    MapBasedPersistence<ProtoSchema.User> users = new MapBasedPersistence<ProtoSchema.User>();
    users.mutate("joe", Functions.constant(
        User.newBuilder()
        .setEmail("joe@example.com")
        .setPassword("password")
        .setUsername("joe")
        .build()));
    users.mutate("jim", Functions.constant(
        User.newBuilder()
        .setEmail("jim@example.com")
        .setPassword("password")
        .setUsername("jim")
        .build()));
    return new PersistenceORM(
        new MapBasedPersistence<ProtoSchema.GameInfo>(),
        new MapBasedPersistence<ProtoSchema.GameSummary>(),
        new MapBasedPersistence<ProtoSchema.Snapshot>(),
        users);
  }

  @Override
  public List getList(String id, Object parameterObject)
      throws SQLException {
    checkId(id);
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List getList(String id) throws SQLException {
    checkId(id);
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getObject(String id, Object parameterObject)
      throws SQLException {
    checkId(id, ST_SELECT_USER);
    if (ST_SELECT_USER.equals(id)) {
      User result = users.get(((org.jogre.server.data.User) parameterObject).getUsername());
      if (result == null) {
        return null;
      }
      org.jogre.server.data.User asPojo = new org.jogre.server.data.User();
      asPojo.setEmail(result.getEmail());
      asPojo.setPassword(result.getPassword());
      asPojo.setReceiveNewsletter(result.getReceiveNewsletter());
      asPojo.setSecurityAnswer(result.getSecurityAnswer());
      asPojo.setSecurityQuestion(result.getSecurityQuestion());
      asPojo.setUsername(result.getUsername());
      asPojo.setYearOfBirth(result.getYearOfBirth());
      return asPojo;
    }
    return null;
  }

  @Override
  public void update(String id, Object parameterObject)
      throws SQLException {
    checkId(id, ST_ADD_SNAP_SHOT);
    if (ST_ADD_SNAP_SHOT.equals(id)) {  // update the snapshot for a given key
      final org.jogre.server.data.SnapShot snapshot = 
          (org.jogre.server.data.SnapShot) parameterObject;
      snapshots.mutate(
          snapshot.getGameKey(), 
          new Function<ProtoSchema.Snapshot, ProtoSchema.Snapshot>(){
            @Override
            public Snapshot apply(Snapshot original) {
              return ProtoSchema.Snapshot.newBuilder()
                  .setGameKey(snapshot.getGameKey())
                  .setNumOfTables(snapshot.getNumOfTables())
                  .setNumOfUsers(snapshot.getNumOfUsers())
                  .build();
            }
          });
      
    }
    // TODO Auto-generated method stub
  }

  
  @Override
  public void update(String id) throws SQLException {
    checkId(id, ST_DELETE_ALL_SNAP_SHOT);
    LOG.warn("Attempt submitted to clear the entire store, which is not supported!");
  }

}
