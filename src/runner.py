import os
import glob
import subprocess
import argparse
import re
import datetime
import time

parser = argparse.ArgumentParser(description='Execute all AI levels')
parser.add_argument('--s', dest='sa', action='store_true')
parser.add_argument('--m', dest='ma', action='store_true')
parser.add_argument('--p', dest='ma', action='store_true')
parser.add_argument('--a', dest='all', action='store_true')
parser.add_argument('--d', dest='debug', action='store_true')
parser.add_argument('--g', dest='graph', action='store_true')
parser.set_defaults(debug=False, all=False, fosa=False, foma=False, poma=False)

args = parser.parse_args()
from subprocess import Popen, PIPE, STDOUT
all_levels = [file for file in glob.glob('levels/*')]
#print glob.glob('levels/*')
##print all_levels
levels = []

# filter levels
if not args.all:
    if args.fosa:
        levels.extend([file for file in all_levels if re.match(r'levels\\SA.*', file)])
    if args.foma:
        levels.extend([file for file in all_levels if re.match(r'levels\\MA.*', file)])
    if args.poma:
        levels.extend([file for file in all_levels if re.match(r'levels\\MA.*', file)])
else:
    levels = all_levels
levels= all_levels
##print levels
##levels = ['complevels/FOSAteam42.lvl']
    
if not os.path.exists('logs'):
    os.makedirs('logs')

print('hola')
f= open('results.txt', 'w')
for level in levels:
	k="logs\\" + level[7:-4]
	cmd = "java -jar server.jar -l %s -c \"java multiagent.CentralPlanner %s\"  "% (level,k)
	print (cmd)
	utc_datetime = datetime.datetime.utcnow()
	formated_string = utc_datetime.strftime("%Y-%m-%d-%H%MZ")
	print '[%s] executing %s ...' % (utc_datetime, level), cmd
	ret = subprocess.Popen(cmd, shell = True,  stdin=PIPE, stdout=PIPE, stderr=STDOUT)

	# timeout =time.time() + 20
	out,error = ret.communicate()
	print(out)	
	if 'success' in out:
		print("Double sucess")
		f.write(level + " success\n")
	else:
		print('not success')
		f.write(level + " fail\n")
	# while True:
	# 	if timeout<time.time():
	# 		break

	##ret = subprocess.call(cmd, shell = True)
	subprocess.call('echo %time%', shell = True)
f.close()