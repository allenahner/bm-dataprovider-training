# Obtain target version from Gemfile.lock
locked_bundler_version = '1.13.6'
GEMFILE_DOT_LOCK="Gemfile.lock"
if File.exist? GEMFILE_DOT_LOCK
    lock_file_version = IO.readlines(GEMFILE_DOT_LOCK)[-1].strip
    locked_bundler_version = lock_file_version if /^[\s\d.]+$/ =~ lock_file_version
end

# Ensure target version is installed
local_gems = Gem::Specification.sort_by{ |g| [g.name.downcase, g.version] }.group_by{ |g| g.name }
missing_bundler_version = true
local_gems["bundler"].each do |gem|
    if gem.version == Gem::Version.new(locked_bundler_version)
        missing_bundler_version = false
    end
end
if missing_bundler_version
    puts "Installing required Bundler version #{locked_bundler_version}"
    system('bash', '-c', "gem install bundler -v #{locked_bundler_version}")
end

# Re-exec with target version
if $PROGRAM_NAME.include? 'rake' or $PROGRAM_NAME.include? 'bundle'
    unless Gem::Version.new(Bundler::VERSION) == Gem::Version.new(locked_bundler_version)
        puts "Locked Bundler version (#{locked_bundler_version}) does not match current Bundler version (#{Gem::Version.new(Bundler::VERSION)}).\nRe-invoking with locked Bundler version (#{locked_bundler_version})."
        command = ''
        command += "bundle _#{locked_bundler_version}_ exec " unless $PROGRAM_NAME.include? 'bundle' or $PROGRAM_NAME.include? 'ruby_executable_hooks'
        command += $PROGRAM_NAME + ' ' unless $PROGRAM_NAME.include? 'ruby_executable_hooks'
        command += "_#{locked_bundler_version}_ " if $PROGRAM_NAME.include? 'bundle'
        command += ARGV.join(' ')
        puts "Re-invoking '#{command}'"
        exec('bash', '-c', command)
    end
end

puts "Using Bundler version: #{Gem::Version.new(Bundler::VERSION)}"
# Include tooling Gemfiles
if File.exist?(File.join('ex-uno-build-tools', 'Gemfile'))
    eval(File.read(File.join('ex-uno-build-tools', 'Gemfile')), nil, File.join('ex-uno-build-tools', 'Gemfile'))
end

if File.exist?(File.join('test-runner', 'Gemfile'))
    eval(File.read(File.join('test-runner', 'Gemfile')), nil, File.join('test-runner', 'Gemfile'))
end

source 'https://rubygems.org' do
    # Add Gems Here
end
