# WordPress-Login-Flow-Android

A pluggable WordPress login flow for Android.

## Usage ##

To use this library in your project, you must set it up as a subtree.
From the root of your main project, add the subtree:

    $ git subtree add --prefix=libs/login git@github.com:wordpress-mobile/WordPress-Login-Flow-Android.git develop

This will create a new directory, `libs/login`, containing the contents of this repository.

Next, you need to generate the `gradle.properties` file:

    $ cp libs/login/gradle.properties-example libs/login/gradle.properties

Configure the fields in `gradle.properties` to match the `compileSdkVersion`,
`buildToolsVersion`, `targetSdkVersion`, and support library versions used
by your project.

## Contributing ##

You can fetch the latest changes made to this library into your project using:

    $ git subtree pull --prefix=libs/login git@github.com:wordpress-mobile/WordPress-Login-Flow-Android.git develop --squash

And you can push your own changes upstream to `WordPress-Login-Flow-Android` using:

    $ git subtree push --prefix=libs/login git@github.com:wordpress-mobile/WordPress-Login-Flow-Android.git branch-name

Note: You can add this repository as a remote to simplify the `git subtree push`/`pull` commands:

    $ git remote add loginlib git@github.com:wordpress-mobile/WordPress-Login-Flow-Android.git

This will allow to use this form instead:

    $ git subtree pull --prefix=libs/login loginlib develop --squash

## License ##

WordPress-Login-Flow-Android is an Open Source project covered by the
[GNU General Public License version 2](LICENSE.md).
