Fully Reflective Execution Environments: Towards a New Generation of Virtual Machines
=============================================================================================

Introduction
------------

This branch contains the version of TruffleMate used for executing the experiments in the paper submitted to the [Journal of Systems and Software][JSS]. 

Obtaining and Running TruffleMate
--------------------------------

To checkout the code:

    git clone https://github.com/charig/TruffleMate.git directoryName
    cd directoryName
    git checkout papers/JSS2016

Then, TruffleMate can be build with Ant:

    ant jar or ant compile

Afterwards, the tests can be executed with:

    ./som -G -cp Smalltalk:TestSuite TestHarness (Mate disabled mode)
    ./som -G --mate -activateMate -cp Smalltalk:TestSuite:Smalltalk/Mate:Smalltalk/Mate/MOP TestHarness (Mate enabled)
   
A simple Hello World program is executed with:

    ./som -G --mate -activateMate -cp Smalltalk:TestSuite:Smalltalk/Mate:Smalltalk/Mate/MOP:Examples Hello

The --mate -activateMate options runs the system with the Mate MOP enabled. Note that the classpath must be also extended with the MOP classes found under Smalltalk/MATE. The -G runs TruffleMATE in interpreter mode. For running in combination with GRAAL install graal and remove the -G option.

When working on TruffleMate, for instance in Eclipse, it is helpful to download
the source files for Truffle as well:

    ant develop

Information on previous authors are included in the AUTHORS file. This code is
distributed under the MIT License. Please see the LICENSE file for details.

Running the experiments
-------------------------

[Click here](Documentation/Experiments.md) for a detailed description of every benchmark and instructions on how to run them

Build Status
------------

Thanks to Travis CI, all commits of this repository are tested.
The current build status is: [![Build Status](
https://travis-ci.org/charig/TruffleMATE.png)](https://travis-ci.org/charig/TruffleMATE)

 [JSS]: http://www.journals.elsevier.com/journal-of-systems-and-software
