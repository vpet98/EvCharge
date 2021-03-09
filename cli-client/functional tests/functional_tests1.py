#!/usr/bin/env python3

from click.testing import CliRunner
import ev_group46
import urllib3
urllib3.disable_warnings()

def test_resetsessions():
	runner = CliRunner()
	result = runner.invoke(ev_group46.resetsessions)
	assert result.exit_code == 0
	assert result.output == 'all the sessions in the database have been reset\n'
	
def test_healthcheck():
	runner = CliRunner()
	result = runner.invoke(ev_group46.healthcheck)
	assert result.exit_code == 0
	assert result.output == 'we are connected with the database\n'
	

