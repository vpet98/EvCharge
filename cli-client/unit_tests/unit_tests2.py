#!/usr/bin/env python3

from click.testing import CliRunner
import ev_group46
import urllib3
import unittest
from mock import Mock, patch
import requests
import requests_mock
import os
from os.path import expanduser
import json
urllib3.disable_warnings()

status = {}
status['status']='OK'

login = {}
login['token'] = 'aaaa'

home = expanduser("~")	#https://stackoverflow.com/questions/4028904/how-to-get-the-home-directory-in-python		
path_of_token = "%s/softeng20bAPI.token" % home

newuser = {}
newuser['message'] = 'aa'

users = {}
users['Username'] = 'aaaaa'
users['Token'] = 'aaaa'

@requests_mock.mock()
def test1_healthcheck(requests_mock):
	requests_mock.get('https://localhost:8765/evcharge/api/admin/healthcheck', json = status)
	#print(ev_group46.healthcheck())
	assert ev_group46.healthcheck() == {'status' : 'OK'}
	
	
@requests_mock.mock()
def test2_resetsessions(requests_mock):
	requests_mock.post('https://localhost:8765/evcharge/api/admin/resetsessions', json = status)
	assert ev_group46.resetsessions() == {'status' : 'OK'}

@requests_mock.mock()
def test3_login(requests_mock):
	requests_mock.post('https://localhost:8765/evcharge/api/login', json = login, status_code = 200)
	#print (ev_group46.Login('admin', 'petrol4ever'))
	assert ev_group46.Login('admin', 'petrol4ever') == 200
		
@requests_mock.mock()
def test4_admin_add_user(requests_mock):
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	requests_mock.post('https://localhost:8765/evcharge/api/admin/usermod/' + 'aaaaa' + '/' + '1111', json = newuser, status_code = 200)
	#print(ev_group46.Admin(True, 'aaaaa' , '1111' , False, False, None, False, False, apikey))
	assert ev_group46.Admin(True, 'aaaaa' , '1111' , False, False, None, False, False, apikey) == 200

@requests_mock.mock()
def test5_admin_users(requests_mock):
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	requests_mock.get('https://localhost:8765/evcharge/api/admin/users/' + 'aaaaa', json = users, status_code = 200)
	#print(ev_group46.Admin(True, 'aaaaa' , '1111' , False, False, None, False, False, apikey))
	assert ev_group46.Admin(False, None , None , 'aaaaa', False, None, False, False, apikey) == 200

@requests_mock.mock()
def test6_logout(requests_mock):
	token_file = open(path_of_token, 'r')
	tok = json.load(token_file)
	apikey = tok['token']
	requests_mock.post('https://localhost:8765/evcharge/api/logout', json = login, status_code = 200)
	assert ev_group46.logout(apikey) == 200

