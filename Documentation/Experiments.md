What They Do, and How to Run the Experiments 
=============================================

General Instructions
---------------------

The command for running all the experiments always include the -G option. This runs TruffleMate in interpreted mode. We strongly recommend to run the experiments with just in time compilation enabled. To run in this mode, TruffleMate depends on the [Graal VM](http://www.oracle.com/technetwork/oracle-labs/program-languages/overview/index-2301583.html). We suggest to download the last precompiled binaries from the official oracle site and then configure TruffleMate's [executable script](https://github.com/charig/TruffleMATE/blob/papers/JSS2016/som) so that it points to the right binary. Then just remove the -G option from every command.   

Inmmutability 
-------------
The metaobjects for realizing both kinds of immutability are described in the paper in Sections 6.2.1 and 6.2.2.

Since the metaobject for reference immutability contains the metaobject for the more simpler object immutability we provide only experimentation for reference immutability. To make the experimentation more complete we also provide an implementation of reference immutability based on [Delegation Proxies](http://dl.acm.org/citation.cfm?id=2577081).

In the example we walk through an (immutable) linked-list of Points and intend to modify the x value of each. To assess the propagation of the immutability property, we also try to modify the x value of the n next elements of the list, being n a customizable parameter. 

The base class describing the experiment is [SumKeys](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Immutability/SumKeys.som). This class runs the example allowing the modifications to the list elements. [ReadonlySumKeys](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Immutability/ReadonlySumKeys.som) and [DelegationProxiesSumKeys](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Immutability/DelegationProxiesSumKeys.som) 
run the experiments with *[handles](http://dl.acm.org/citation.cfm?id=1894393)* implemented with metaobjects and with delegation proxies respectively.

The [folder](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Immutability/) contains additional files describing the metaobjects and auxiliary classes. 

Finally, the experiment based on metaobjects runs with:
      
      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Examples/Benchmarks:Examples/Benchmarks/Mate:Examples/Benchmarks/Mate/Immutability:Examples/Benchmarks/Mate/Immutability/DelegationProxies:Examples/Benchmarks/Mate/Immutability/Handles BenchmarkHarness ReadonlySumKeys 1 0 1
      
Changing *ReadonlySumKeys* for *SumKeys* or *DelegationProxiesSumKeys* run the experiment with the aforementioned alternatives.       


Profiling
---------
The experiment in the paper is described in Section 6.2.3.

Columnar Layouts
----------------
The metaobjects for realizing a columnar-object behavior for classes is described in the paper in Section 6.3.1.

In this example we experiment with the metaobjects by analyzing them in the context of an analytical algorithmn. Concretely, the experiment loads an open-source [dataset](http://grouplens.org/datasets/movielens/) in memory. This dataset contains ratings of movies from different users submitted to the [MovieLens](https://movielens.org/) website. 

The base (abstract) class describing the experiment is [Movies](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/Movies.som).

The concrete files for running the experiment with the standard behavior are: [MoviesAggregate](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesAggregate.som), [MoviesFilter](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesFilter.som), [MoviesMap](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesMap.som). Essentially, each iterates through all the records and execute an aggregate, filter or map operation respectively.

The files for running the experiment with the columnar layout are [MoviesColumnarAggregate](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesColumnarAggregate.som), [MoviesColumnarFilter](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesColumnarFilter.som), [MoviesColumnarMap](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/MoviesColumnarMap.som)

The [folder](https://github.com/charig/SOM/blob/papers/JSS2016/Examples/Benchmarks/Mate/Columnar/) contains additional files describing the metaobjects and auxiliary classes. 

Finally, the columnar experiment for the aggregate operation runs with:
      
      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Smalltalk/Collections/Streams:Smalltalk/FileSystem/Core:Smalltalk/FileSystem/Disk:Smalltalk/FileSystem/Streams:Examples/Benchmarks:Examples/Benchmarks/Mate/Columnar BenchmarkHarness MoviesColumnarAggregate 1 0 1

Do not forget to download the movielens file and change the path in the *loadMovies* method to point to the right *ratings.dat* file! For running the experiments for other operations or without a columnar layout just change the name of the Movies file. 


Hash-based vs. Array-based Layouts
----------------------------------
The experiment in the paper is described in Section 6.3.2
