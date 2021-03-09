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
		
def test_admin_healthcheck():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.Admin, ['--healthcheck', '--apikey', apikey])
	#print(result.output)
	assert result.exit_code == 0
	assert result.output == 'we are connected with the database\n'

def test_logout2():
	runner = CliRunner()
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	result = runner.invoke(ev_group46.logout, ['--apikey', apikey])
	assert result.exit_code == 0
	assert result.output == 'logout was successful\n'

