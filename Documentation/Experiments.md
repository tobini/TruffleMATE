What They Do, and How to Run the Experiments 
=============================================

General Instructions
---------------------

The command for running all the experiments always includes the -G option. This runs TruffleMate in interpreted mode. We strongly recommend to run the experiments with just in time compilation enabled. To do so, TruffleMate requires the installation of the [Graal compiler](http://www.oracle.com/technetwork/oracle-labs/program-languages/overview/index-2301583.html). We suggest to download the last binaries from the official oracle site and then configure TruffleMate's [executable script](https://github.com/charig/TruffleMATE/blob/papers/TSE2017/som) so that it points to the right binary. Then just remove the -G option from every command.

Benchmarks are run by a harness. The harness takes three parameters: benchmark name, number of iterations, and problem size. The benchmark name corresponds to a class or file of a benchmark. The number of iterations defines how often a benchmark should be executed. The problem size can be used to influence how long a benchmark takes. Note that the benchmarks just output the time it takes for the machine to run them. However, each benchmark verifies the result (see the method *verifyResult*) and throws an error in case the results is different from the expected one. Note also that some benchmarks rely on magic numbers to verify their results.   

Inmmutability 
-------------
The metaobjects for realizing both kinds of immutability are described in the paper in Sections 6.2.1 and 6.2.2.

Since the metaobject for reference immutability contains the metaobject for the more simpler object immutability we provide only experimentation for reference immutability. To make the experimentation more complete we also provide an implementation of reference immutability based on [Delegation Proxies](http://dl.acm.org/citation.cfm?id=2577081).

In the example we walk through an (immutable) linked-list of Points and intend to modify the x value of each. To assess the propagation of the immutability property, we also try to modify the x value of the n next elements of the list, being n a customizable parameter. 

The base class describing the experiment is [SumKeys](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Immutability/SumKeys.som). This class runs the example allowing the modifications to the list elements. [ReadonlySumKeys](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Immutability/ReadonlySumKeys.som) and [DelegationProxiesSumKeys](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Immutability/DelegationProxiesSumKeys.som) 
run the experiments with *[handles](http://dl.acm.org/citation.cfm?id=1894393)* implemented with metaobjects and with delegation proxies respectively.

The [folder](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Immutability/) contains additional files describing the metaobjects and auxiliary classes. 

Finally, the experiment based on metaobjects runs with:
      
      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Examples/Benchmarks:Examples/Benchmarks/Mate/Immutability:Examples/Benchmarks/Mate/Immutability/DelegationProxies:Examples/Benchmarks/Mate/Immutability/Handles BenchmarkHarness ReadonlySumKeys 1 0 1
      
Changing *ReadonlySumKeys* for *SumKeys* or *DelegationProxiesSumKeys* run the experiment with the aforementioned alternatives.       


Profiling
---------
The metaobjects for profiling the system are described in the paper in Section 6.2.3.

This experiment mainly exploits TruffleMATE's reflective capabilities to collect the low-level information that a tool like [Senseo] (http://scg.unibe.ch/research/senseo)(a dynamic behavior visualization tool) requires for producing its output. Actually, Senseo depends on third-party tools like Aspect-Oriented dynamic weavers for collecting that information.

The [folder](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Profiling/) contains two files corresponding to the implementation of the [Calling Context Tree](http://dl.acm.org/citation.cfm?id=258924) data structure: [CCT](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Profiling/CCT.som) and [CallRecord](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Profiling/CallRecord.som). It also contains the metaobject intercepting all method activations so that it logs the needed information in the CCT: [CCTActivationSemantics](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Profiling/CCTActivationSemantics.som)

To profile 100 iterations of the DeltaBlue macrobenchmark:

      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Examples/Benchmarks:Examples/Benchmarks/Mate:Examples/Benchmarks/Mate/Profiling BenchmarkHarness DeltaBlueProfiled 100 0 10


Columnar Layouts
----------------
The metaobjects for realizing a columnar-object behavior for classes is described in the paper in Section 6.3.1.

In this example we experiment with the metaobjects by analyzing them in the context of an analytical algorithmn. Concretely, the experiment loads an open-source [dataset](http://grouplens.org/datasets/movielens/) in memory. This dataset contains ratings of movies from different users submitted to the [MovieLens](https://movielens.org/) website. 

The base (abstract) class describing the experiment is [Movies](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/Movies.som).

The concrete files for running the experiment with the standard behavior are: [MoviesAggregate](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesAggregate.som), [MoviesFilter](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesFilter.som), [MoviesMap](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesMap.som). Essentially, each iterates through all the records and execute an aggregate, filter or map operation respectively.

The files for running the experiment with the columnar layout are [MoviesColumnarAggregate](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesColumnarAggregate.som), [MoviesColumnarFilter](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesColumnarFilter.som), [MoviesColumnarMap](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/MoviesColumnarMap.som)

The [folder](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Columnar/) contains additional files describing the metaobjects and auxiliary classes. 

Finally, the columnar experiment for the aggregate operation runs with:
      
      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Smalltalk/Collections/Streams:Smalltalk/FileSystem/Core:Smalltalk/FileSystem/Disk:Smalltalk/FileSystem/Streams:Examples/Benchmarks:Examples/Benchmarks/Mate/Columnar BenchmarkHarness MoviesColumnarAggregate 1 0 1

Do not forget to download the movielens file and change the path in the *loadMovies* method to point to the right *ratings.dat* file! For running the experiments for other operations or without a columnar layout just change the name of the Movies file. 


Hash-based vs. Array-based Layouts
----------------------------------
The metaobject for realizing a hash-based layout is described in the paper in Section 6.3.2.

In this experiment we show how to change the layout of objects on-the-fly. Concretely, for a [Person](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Layout/Person.som) class with 10 fields, we create 20 instances and fill only 3 of those fields. Afterwards, for only 10 of those 20 instances, we change the layout so that it only stores 6 fields and behaves in a hash-based manner. Accordingly, we also attach a [metaobject](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Layout/HashFieldsSemanticsMO.som) to these 10 instances providing the required hash-based behavior for field accessing.

The [folder](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Layout/) contains the [base class](https://github.com/charig/SOM/blob/papers/TSE2017/Examples/Benchmarks/Mate/Layout/HashBasedLayout.som) for the experiment, along with the Person class and the aforementioned metaobject. 

To run the experiment:

      ./som -G --mate -activateMate -cp Smalltalk:Smalltalk/Mate/:Smalltalk/Mate/MOP:Examples/Benchmarks:Examples/Benchmarks/Mate:Examples/Benchmarks/Mate/Layout BenchmarkHarness HashBasedLayout 1 0 1
