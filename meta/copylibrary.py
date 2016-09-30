#!/usr/bin/python

import os

TARGET = '/home/lelloman/AndroidStudioProjects/EasyTranscription/app/libs/lousyaudiolibrary.aar'
SOURCE = '/home/lelloman/AndroidStudioProjects/LousyAudioLibrary/library/build/outputs/aar/library-release.aar'

if os.path.isfile(TARGET):
	os.remove(TARGET)

with open(SOURCE, 'rb') as source:
	with open(TARGET, 'wb') as target:
		target.write(source.read())

