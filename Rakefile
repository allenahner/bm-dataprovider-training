require 'rubygems'
require 'bundler/setup'

RAKE_ROOT = File.expand_path(File.dirname(__FILE__))
SRC_DIR = "#{RAKE_ROOT}/src"
JAR_ROOT = "#{RAKE_ROOT}/build/jar"
PROJECT_FOLDER = "#{RAKE_ROOT.split("/").last}"

import "#{RAKE_ROOT}/ex-uno-build-tools/rake/helpers.rake"
import "#{RAKE_ROOT}/ex-uno-build-tools/rake/init.rake"
import "#{RAKE_ROOT}/ex-uno-build-tools/rake/reopens.rake"
Dir.glob("#{RAKE_ROOT}/ex-uno-build-tools/rake/*.rake").each { |r| import r }
Dir.glob("#{RAKE_ROOT}/test-runner/rake/*.rake").each { |r| import r }
Dir.glob("#{RAKE_ROOT}/rake/*.rake").each { |r| import r }
