package org.jogre.server.data.db;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.jogre.server.data.ProtoSchema;
import org.jogre.server.data.ProtoSchema.GameInfo;
import org.jogre.server.data.ProtoSchema.GameSummary;
import org.jogre.server.data.ProtoSchema.Snapshot;
import org.jogre.server.data.ProtoSchema.User;

import com.appenginefan.toolkit.persistence.MapBasedPersistence;
import com.appenginefan.toolkit.persistence.Persistence;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;

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
    // TODO: is this method ever used?
    return null;
  }

  @Override
  public List getList(String id) throws SQLException {
    checkId(id);
    // TODO: is this method ever used?
    return null;
  }

  @Override
  public Object getObject(String id, Object parameterObject)
      throws SQLException {
    checkId(id, ST_SELECT_USER, ST_SELECT_GAME_SUMMARY);
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
    if (ST_SELECT_GAME_SUMMARY.equals(id)) {
      String key = makeKey((org.jogre.server.data.GameSummary) parameterObject);
      GameSummary summary = summaries.get(key);
      if (summary == null) {
        return null;
      }
      org.jogre.server.data.GameSummary asPojo = new org.jogre.server.data.GameSummary();
      asPojo.setDraws(summary.getDraws());
      asPojo.setGameKey(summary.getGameKey());
      asPojo.setLoses(summary.getLoses());
      asPojo.setRating(summary.getRating());
      asPojo.setStreak(summary.getStreak());
      asPojo.setUsername(summary.getUsername());
      asPojo.setWins(summary.getWins());
      return asPojo;
    }
    return null;
  }
  
  @Override
  public void update(String id, Object parameterObject)
      throws SQLException {
    checkId(id, ST_ADD_SNAP_SHOT, ST_ADD_GAME_SUMMARY, ST_UPDATE_SNAP_SHOT, ST_ADD_GAME_INFO, ST_UPDATE_GAME_SUMMARY);
    if (ST_ADD_SNAP_SHOT.equals(id) || ST_UPDATE_SNAP_SHOT.equals(id)) {
      final org.jogre.server.data.SnapShot snapshot = 
          (org.jogre.server.data.SnapShot) parameterObject;
      snapshots.mutate(
          snapshot.getGameKey(),
          Functions.constant(
              ProtoSchema.Snapshot.newBuilder()
                  .setGameKey(snapshot.getGameKey())
                  .setNumOfTables(snapshot.getNumOfTables())
                  .setNumOfUsers(snapshot.getNumOfUsers())
                  .build()));
      return;
    }
    if (ST_ADD_GAME_SUMMARY.equals(id) || ST_UPDATE_GAME_SUMMARY.equals(id)) {
      final org.jogre.server.data.GameSummary summary = 
          (org.jogre.server.data.GameSummary) parameterObject;
      summaries.mutate(
          makeKey(summary),
          Functions.constant(
              ProtoSchema.GameSummary.newBuilder()
              .setDraws(summary.getDraws())
              .setLoses(summary.getLoses())
              .setRating(summary.getRating())
              .setStreak(summary.getStreak())
              .setWins(summary.getWins())
              .setUsername(summary.getUsername())
              .setGameKey(summary.getGameKey())
              .build()));
      return;
    }
    if (ST_ADD_GAME_INFO.equals(id)) {
      final org.jogre.server.data.GameInfo info = 
          (org.jogre.server.data.GameInfo) parameterObject;
      infos.mutate(
          String.valueOf(info.getId()),
          Functions.constant(
              ProtoSchema.GameInfo.newBuilder()
              .setEndTime(info.getEndTime().getTime())
              .setGameKey(info.getGameKey())
              .setHistory(info.getGameHistory())
              .setId(info.getId())
              .setPlayers(info.getPlayers())
              .setResults(info.getResults())
              .setScore(info.getGameScore())
              .setStartTime(info.getStartTime().getTime())
              .build()));
      return;
    }
  }

  
  @Override
  public void update(String id) throws SQLException {
    checkId(id, ST_DELETE_ALL_SNAP_SHOT);
    LOG.warning("Attempt submitted to clear the entire store, which is not supported!");
  }
  
  private String makeKey(org.jogre.server.data.GameSummary summary) {
    return summary.getGameKey() + "::" + summary.getUsername();
  }

}
