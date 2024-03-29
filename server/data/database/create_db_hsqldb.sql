-- ===============================================================
-- 
-- Script file for creating a jogre database for storing server 
-- persistent data for HSQLDB database.
-- 
-- Author: Bob Marks
-- Date:   2nd March 2006
-- 
-- NOTE: There are no commands in the HSQLDB for dropping or
-- creating databases.
--
-- ===============================================================

-- 1) drop tables (if they exist)

drop table if exists game_info;
drop table if exists game_summary;
drop table if exists snap_shot;
drop table if exists user;

-- 2) Create tables

create table game_info (id integer generated by default as identity (start with 1), game_key char(20), players char (255), results char (50), start_time datetime, end_time datetime, history char (255), score char (255), primary key (id));
create table game_summary (game_key char(20), username char (20), rating integer, wins integer, loses integer, draws integer, streak integer, primary key (game_key, username));
create table snap_shot (game_key char(20), num_of_users integer, num_of_tables integer, primary key (game_key));
create table user (username char(20), password char (20), security_question integer, security_answer char (50), year_of_birth char (4), email char (100), receive_newsletter char (1), primary key (username));

-- 3) Insert dummey data into tables to get started

insert into user (username, password, security_question, security_answer, year_of_birth, email, receive_newsletter) values ('bob', 'bob123', 0, '', 1999, '', 'n'); 
insert into user (username, password, security_question, security_answer, year_of_birth, email, receive_newsletter) values ('dave', 'dave123', 0, '', 1999, '', 'n'); 
insert into user (username, password, security_question, security_answer, year_of_birth, email, receive_newsletter) values ('john', 'john123', 0, '', 1999, '', 'n');
insert into user (username, password, security_question, security_answer, year_of_birth, email, receive_newsletter) values ('sharon', 'sharon123', 9, '', 1999, '', 'n');