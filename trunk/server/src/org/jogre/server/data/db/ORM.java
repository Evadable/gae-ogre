package org.jogre.server.data.db;

import java.sql.SQLException;
import java.util.List;

/**
 * Represents lower-level logic that translates object data into the store layer.
 * 
 * @author Jens Scheffler
 *
 */
public interface ORM {

  /**
   * Return a single object with supplied parameter object.
   * 
   * @param id
   * @param parameterObject
   * @return
   */
  public abstract Object getObject(String id,
      Object parameterObject) throws SQLException;

  /**
   * Return object.
   * 
   * @param id
   * @return
   * @throws SQLException
   */
  public abstract Object getObject(String id)
      throws SQLException;

  /**
   * Return a long value from an SQL map.  Useful for getting primary IDs.
   * 
   * @param id                  Statement ID.
   */
  public abstract long getLong(String id)
      throws SQLException;

  /**
   * Return list of object with parameter object.
   * 
   * @param id
   * @param parameterObject
   * @return
   */
  public abstract List getList(String id,
      Object parameterObject) throws SQLException;

  /**
   * Return list of object with no parameter object.
   * 
   * @param id
   * @return
   */
  public abstract List getList(String id)
      throws SQLException;

  /**
   * Update object using parameter object.
   * 
   * @param id                  Statement ID.
   * @param parameterObject     Parameter object.
   */
  public abstract void update(String id,
      Object parameterObject) throws SQLException;

  /**
   * Update database with no parameter object.
   * 
   * @param id
   * @throws SQLException
   */
  public abstract void update(String id)
      throws SQLException;

}