#!/usr/bin/env python3

import click 
import requests
import pprint
import json
import os
from os.path import expanduser
import re
import urllib3
urllib3.disable_warnings()
from click_option_group import optgroup, RequiredMutuallyExclusiveOptionGroup, RequiredAnyOptionGroup

import contextlib

@contextlib.contextmanager
def pprint_nosort():
    # Note: the pprint implementation changed somewhere
    # between 2.7.12 and 3.7.0. This is the danger of
    # monkeypatching!
    try:
        # Old pprint
        orig,pprint._sorted = pprint._sorted, lambda x:x
    except AttributeError:
        # New pprint
        import builtins
        orig,pprint.sorted = None, lambda x, key=None:x

    try:
        yield
    finally:
        if orig:
            pprint._sorted = orig
        else:
            del pprint.sorted


regex = '^[a-z0-9]{4}[-]{1}[a-z0-9]{4}[-]{1}[a-z0-9]{4}$'
password_regex = '[^ ]+'
'''
def check_api_key(api_key) :
	if (re.search(regex, api_key, flags = re.IGNORECASE)):
		return True
	else:
		return False
'''

def check_password(password) :
	if (" " not in password):
		return True
	else:
		return False

    
    
def healthcheck(format = 'json'):
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
	else:
		url = 'https://localhost:8765/evcharge/api/admin/healthcheck'
		res = requests.get(url, verify = False)
		res = res.json()
		if (res['status'] == 'OK'):
			click.echo("we are connected with the database")
		else :
			click.echo("connection is failed")
		return res
			

def resetsessions(format = 'json'):
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
	else:
		url = 'https://localhost:8765/evcharge/api/admin/resetsessions'
		res = requests.post(url, verify = False)
		res = res.json()
		if (res['status'] == 'OK'):
			if os.path.exists(path_of_token):
				os.remove(path_of_token)
			click.echo("all the sessions in the database have been reset")
		else :
			click.echo("reset is failed")
		return res

home = expanduser("~")	#https://stackoverflow.com/questions/4028904/how-to-get-the-home-directory-in-python		
path_of_token = "%s/softeng20bAPI.token" % home			
    
def Login(username, passw, format = 'json'):
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
	else:
		if os.path.exists(path_of_token):
			pass
			#os.remove(path_of_token)
			#click.echo('the user has been logged in')

		else:
			url = 'https://localhost:8765/evcharge/api/login'
			res = requests.post(url, data={'username': username, 'password': passw}, headers={'Content-Type': 'application/x-www-form-urlencoded'}, verify=False)
			t = res.status_code
			if res.status_code == 200 : 
				res = res.json()
				token_file = open(path_of_token, "w+")
				json.dump(res, token_file)
				token_file.close()
				#print(res)
				#click.echo('login was successful')
			else :
				pass
				#click.echo('login is rejected')
			return t
		    		

  
def logout(apikey, format= 'json'):
	if format not in ['json', 'csv'] :
        	click.echo("not accepted format")
        #elif (not check_api_key(apikey)):
        #	click.echo("not accepted api-key")
	else:
		if os.path.exists(path_of_token):
			token_file = open(path_of_token, 'r')
			tok = json.load(token_file)
			url = 'https://localhost:8765/evcharge/api/logout'
			#print(tok)
			cont = 'Bearer ' + tok['token']
			res = requests.post(url, headers= {'X-OBSERVATORY-AUTH': cont}, verify=False)
			token_file.close()
			#print(res)
			if res.status_code==200:
				os.remove(path_of_token)
				click.echo('logout was successful')
			else:
		    		click.echo('logout is rejected')
			return res.status_code
		else:
			click.echo('the user is not logged in')
			



def Admin(usermod, username, passw, users, sessionsupd, source, healthcheck, resetsessions, apikey, format= 'json'):
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
        	return
	if (os.path.exists(path_of_token)):
		token_file = open(path_of_token, 'r')
		tok = json.load(token_file)
		#print(tok)
		if (tok['token'] != apikey): 
			click.echo("you are not authenticated to make changes")
			return
		token_file.close()
		if (healthcheck) : 
			url = 'https://localhost:8765/evcharge/api/admin/healthcheck'
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': tok['token']}, verify = False)
			res = res.json()
			if (res['status'] == 'OK'):
				click.echo("we are connected with the database")
			elif res.status_code == 401 :
				click.echo("you are not authenticated to make changes")
			else :
				click.echo("connection is failed")
			#click.echo("healthcheck: done")
		if (resetsessions) : 
			url = 'https://localhost:8765/evcharge/api/admin/resetsessions'
			res = requests.post(url, headers={'X-OBSERVATORY-AUTH': tok['token']}, verify = False)
			res = res.json()
			if (res['status'] == 'OK'):
				if os.path.exists(path_of_token):
					os.remove(path_of_token)
				click.echo("all the sessions in the database have been reset")
			elif res.status_code == 401 :
				click.echo("you are not authenticated to make changes")
			else :
				click.echo("reset is failed")
			click.echo("resetsessions: done")
		if (usermod): 
			#url = 'https://localhost:8765/evcharge/api/admin/users'
			'''
			if (username != None) : 
				res = requests.get(url, verify = False)
				res = res.json()
				userexist = False
				for user in res['username']:
					if (username == user) :
						userexist = True
						break
				if (userexist) : 
					if (username != None and passw != None):
						if (check_password(passw) == False):
							click.echo("password should not have spaces")
						else :
							urlnew = 'https://localhost:8765/evcharge/api/admin/:' + username
							requests.delete(urlnew, verify=False)
							requests.put(urlnew, verify=False)
					else : 
						click.echo("username and password are required for this modification")
					if (passw != None and check_password(passw)) : 
						urlnew = 'https://localhost:8765/evcharge/api/admin/:' + username
						res = requests.put(urlnew, data = passw, verify=False)
					else :
						click.echo("not accepted changed password")
				else : 
					if (passw != None and check_password(passw)) : 
						urlnew = 'https://localhost:8765/evcharge/api/admin/:' + username
						res = requests.post(urlnew, data = passw, verify=False)
					else :
						click.echo("not accepted new password")
			'''
			
			if (username != None and passw != None):
				if (check_password(passw) == False):
					click.echo("password should not have spaces")
				else :
					url = 'https://localhost:8765/evcharge/api/admin/usermod/' + username + '/' + passw
					#url = 'https://localhost:8765/evcharge/admin'
					#print (url)
					cont = 'Bearer ' + tok['token']
					#print(cont)
					res = requests.post(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
					#res = res.json()
					t = res.status_code
					if res.status_code == 200 : 
						res = res.json()
						click.echo(res['message'])
						click.echo("usermod : done")
						
					elif res.status_code == 401 :
						click.echo("you are not authenticated to make changes")
						
					else : 
						click.echo("error in usermod")
					return t
					#res = res.json()
					#print(res)
			else : 
				click.echo("username and password are required for this modification")
			
		if (users != None):
			user_found = users
			url = 'https://localhost:8765/evcharge/api/admin/users/' + user_found
			cont = 'Bearer ' + tok['token']
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
			#info = res.text
			#print(res)
			t = res.status_code
			if res.status_code == 200 : 
				res = res.json()
				#print(res)
				print("username = ", res['Username'])
				print("token = ", res['Token'])
				click.echo("users : done")
			elif res.status_code == 401 :
				click.echo("you are not authenticated to make changes")
			else : 
				click.echo("user doesnt exist")
			return t
				
		if (sessionsupd) : 
			if (source != None) :
				url = 'https://localhost:8765/evcharge/api/admin/system/sessionsupd'
				cont = 'Bearer ' + tok['token']
				res = requests.post(url, files = {'file' : open(source, 'rb')}, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
				#info = res.text
				print(res.json())
				if res.status_code == 200 : 
					res = res.json()
					click.echo("sessionsupd : done")
				elif res.status_code == 401 :
					click.echo("you are not authenticated to make changes")
				else : 
					click.echo("error in sessionsupd")
			else : 
				click.echo("no file found to be uploaded")
	else : 
		print("hi")
		click.echo("you are not authenticated to make changes")
		
		

def SessionsPerPoint(point, datefrom, dateto, apikey, format= 'json' ):		
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
        	return
	if (format == 'csv') :
		url = 'https://localhost:8765/evcharge/api/SessionsPerPoint/' + point + '/' + datefrom + '/' + dateto + '?format=csv'
	else : 
		url = 'https://localhost:8765/evcharge/api/SessionsPerPoint/' + point + '/' + datefrom + '/' + dateto
	if (os.path.exists(path_of_token)):
		token_file = open(path_of_token, 'r')
		tok = json.load(token_file)
		#print(tok)
		if (tok['token'] != apikey): 
			click.echo("you have to be a logged in user")
		else : 
			cont = 'Bearer ' + tok['token']
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
			if res.status_code == 200 : 
				if (format == 'json') :
					res = res.json()
					with pprint_nosort():
						pprint.pprint(res)
				else : 
					click.echo(res.text)
			else : 
				click.echo(res.text)
			
	else : 
		click.echo("you have to be a logged in user")
    

def SessionsPerStation(station, datefrom, dateto, apikey, format= 'json'):		
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
        	return
	if (format == 'csv') :
		url = 'https://localhost:8765/evcharge/api/SessionsPerStation/' + station + '/' + datefrom + '/' + dateto + '?format=csv'
	else : 
		url = 'https://localhost:8765/evcharge/api/SessionsPerStation/' + station + '/' + datefrom + '/' + dateto
	if (os.path.exists(path_of_token)):
		token_file = open(path_of_token, 'r')
		tok = json.load(token_file)
		#print(tok)
		if (tok['token'] != apikey): 
			click.echo("you have to be a logged in user")
		else : 
			cont = 'Bearer ' + tok['token']
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
			if res.status_code == 200 : 
				if (format == 'json') :
					res = res.json()
					with pprint_nosort():
						pprint.pprint(res)
				else : 
					click.echo(res.text)
			else : 
				click.echo(res.text)
			
	else : 
		click.echo("you have to be a logged in user")
		

def SessionsPerEV(ev, datefrom, dateto, apikey, format= 'json'):		
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
        	return
	#print(datefrom)
	if (format == 'csv') :
		url = 'https://localhost:8765/evcharge/api/SessionsPerEV/' + ev + '/' + datefrom + '/' + dateto + '?format=csv'
	else : 
		url = 'https://localhost:8765/evcharge/api/SessionsPerEV/' + ev + '/' + datefrom + '/' + dateto
	#print(url)
	if (os.path.exists(path_of_token)):
		token_file = open(path_of_token, 'r')
		tok = json.load(token_file)
		#print(tok)
		if (tok['token'] != apikey): 
			click.echo("you have to be a logged in user")
		else : 
			cont = 'Bearer ' + tok['token']
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
			if res.status_code == 200 : 
				if (format == 'json') :
					res = res.json()
					with pprint_nosort():
						pprint.pprint(res)
				else : 
					click.echo(res.text)
			else : 
				click.echo(res.text)
			
	else : 
		click.echo("you have to be a logged in user")
		

def SessionsPerProvider(provider, datefrom, dateto, apikey, format= 'json'):		
	if format not in ['json', 'csv']:
        	click.echo("not accepted format")
        	return
	if (format == 'csv') :
		url = 'https://localhost:8765/evcharge/api/SessionsPerProvider/' + provider + '/' + datefrom + '/' + dateto + '?format=csv'
	else : 
		url = 'https://localhost:8765/evcharge/api/SessionsPerProvider/' + provider + '/' + datefrom + '/' + dateto
	if (os.path.exists(path_of_token)):
		token_file = open(path_of_token, 'r')
		tok = json.load(token_file)
		#print(tok)
		if (tok['token'] != apikey): 
			click.echo("you have to be a logged in user")
		else : 
			cont = 'Bearer ' + tok['token']
			res = requests.get(url, headers={'X-OBSERVATORY-AUTH': cont}, verify = False)
			if res.status_code == 200 : 
				if (format == 'json') :
					res = res.json()
					with pprint_nosort():
						pprint.pprint(res)
				else : 
					click.echo(res.text)
			else : 
				click.echo(res.text)
			
	else : 
		click.echo("you have to be a logged in user")
    

