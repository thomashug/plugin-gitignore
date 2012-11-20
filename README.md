# Forge Gitignore Plugin

This plugin is based on the fabulous [gibo](https://github.com/simonwhitaker/gitignore-boilerplates) 
shell script from Simon Whitaker. It uses the Github Gitignore template repository
to dynamically build a .gitignore file for your project.

## Installation

The plugin is currently installable from this github repository. Start Forge
and run

    forge git-plugin git://github.com/thomashug/plugin-gitignore.git
    
## Usage

The plugin needs to be initialized first:

    git-ignore setup
    
The command will ask you where to install the template file repository
(https://github.com/github/gitignore.git) defaulting to $HOME/.gitignore_boilerplate.
If you have cloned this repository for custom templates, you can also override 
the remote repository when prompted.

Once the templates are downloaded, the list command shows the available templates:

    git-ignore list

You can create a new gitignore with

    git-ignore create ${template1} ${template2} ...
    
Use tab for autocomplete the template names. All selected templates are appended
to .gitignore, so for a Maven project used in Eclipse run e.g.

    git-ignore create Maven Eclipse

From time to time, you might want to update your local definitions. Either use
git pull directly on the template directory, or run

    git-ignore update-repo
    
