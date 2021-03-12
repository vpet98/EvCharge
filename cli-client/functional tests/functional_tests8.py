#!/usr/bin/env python3

from click.testing import CliRunner
import ev_group46
import urllib3
import json
import os
from os.path import expanduser
urllib3.disable_warnings()

home = expanduser("~")	#https://stackoverflow.com/questions/4028904/how-to-get-the-home-directory-in-python		
path_of_token = "%s/softeng20bAPI.token" % home

def test_healthcheck():
	runner = CliRunner()
	result = runner.invoke(ev_group46.healthcheck)
	assert result.exit_code == 0
	assert result.output == 'we are connected with the database\n'
	
def test_login1():
	runner = CliRunner()
	result = runner.invoke(ev_group46.Login, ['--username', 'admin', '--passw' ,'petrol4ever'])
	assert result.exit_code == 0
	assert result.output == 'login was successful\n'
	
def test_newuser1():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.Admin, ['--usermod', '--username', 'group46', '--passw', '12345', '--apikey', apikey])
	print(result.output)
	assert result.exit_code == 0
	assert result.output == 'User registered successfully!\nusermod : done\n'

def sessions_upd():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.Admin, ['--sessionsupd', '--source', 'demo.csv' ,'--apikey', apikey])
	assert result.exit_code == 0
	assert result.output == 'sessionsupd : done\n'
	
def test_logout1():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.logout, ['--apikey', apikey])
	assert result.exit_code == 0
	assert result.output == 'logout was successful\n'

def test_login2():
	runner = CliRunner()
	result = runner.invoke(ev_group46.Login, ['--username', 'group46', '--passw' ,'12345'])
	assert result.exit_code == 0
	assert result.output == 'login was successful\n'
		
def test_SessionsPerPoint1():
	runner = CliRunner()
	result = runner.invoke(ev_group46.SessionsPerPoint, ['--point', '5f6978bb00355e4c01059bc7_5096', '--datefrom', '20190901', '--dateto', '20190902', '--apikey', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYxNTMyMDQ0N30.mwPWMrQ5vlt14aoyoelvtRBUIZPZJbOGDVggV81FC4YxQWlms_6dziLYsf2Q1oPWIIargNIKl2r3k7yZCNbhbA'])
	#print(result.output)
	assert result.exit_code == 0
	assert result.output == 'you have to be a logged in user\n'
	
def test_SessionsPerEV():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.SessionsPerEV, ['--ev', '45b68c71-cd11-4bd7-a03f-fdaae259635d', '--datefrom', '20190901', '--dateto', '20190902', '--format', 'csv', '--apikey', apikey])
	print(result.output)
	assert result.exit_code == 0
	assert '0.746999979019165' in result.output
	
def test_logout2():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.logout, ['--apikey', apikey])
	assert result.exit_code == 0
	assert result.output == 'logout was successful\n'

