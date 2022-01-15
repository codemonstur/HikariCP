/*
 * Copyright (C) 2013 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.hibernate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Stoppable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Connection provider for Hibernate 4.3.
 *
 * @author Brett Wooldridge, Luca Burgazzoli
 */
public class HikariConnectionProvider implements ConnectionProvider, Configurable, Stoppable
{
   private static final long serialVersionUID = -9131625057941275711L;

   /**
    * HikariCP configuration.
    */
   private HikariConfig hcfg;

   /**
    * HikariCP data source.
    */
   private HikariDataSource hds;

   // *************************************************************************
   //
   // *************************************************************************

   /**
    * c-tor
    */
   public HikariConnectionProvider()
   {
      this.hcfg = null;
      this.hds = null;
   }

   // *************************************************************************
   // Configurable
   // *************************************************************************

   @SuppressWarnings("rawtypes")
   @Override
   public void configure(Map props) throws HibernateException
   {
      try {
         this.hcfg = HikariConfigurationUtil.loadConfiguration(props);
         this.hds = new HikariDataSource(this.hcfg);
      }
      catch (Exception e) {
         throw new HibernateException(e);
      }
   }

   // *************************************************************************
   // ConnectionProvider
   // *************************************************************************

   @Override
   public Connection getConnection() throws SQLException
   {
      Connection conn = null;
      if (this.hds != null) {
         conn = this.hds.getConnection();
      }

      return conn;
   }

   @Override
   public void closeConnection(Connection conn) throws SQLException
   {
      conn.close();
   }

   @Override
   public boolean supportsAggressiveRelease()
   {
      return false;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public boolean isUnwrappableAs(Class unwrapType)
   {
      return ConnectionProvider.class.equals(unwrapType) || HikariConnectionProvider.class.isAssignableFrom(unwrapType);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T unwrap(Class<T> unwrapType)
   {
       if ( ConnectionProvider.class.equals( unwrapType ) ||
               HikariConnectionProvider.class.isAssignableFrom( unwrapType ) ) {
           return (T) this;
       }
       else if ( DataSource.class.isAssignableFrom( unwrapType ) ) {
           return (T) this.hds;
       }
       else {
           throw new UnknownUnwrapTypeException( unwrapType );
       }
   }

   // *************************************************************************
   // Stoppable
   // *************************************************************************

   @Override
   public void stop()
   {
      this.hds.close();
   }
}
