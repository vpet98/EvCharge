#!/usr/bin/env python3

from click.testing import CliRunner
import ev_group46
import urllib3
urllib3.disable_warnings()

def test_check_passw1():
	result = ev_group46.check_password("er rre")
	assert result == False

def test_check_passw2():
	result = ev_group46.check_password("flflfflr12345##$!!==rg4+")
	assert result == True
