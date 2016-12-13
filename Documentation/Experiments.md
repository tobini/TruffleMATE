Description on the Content of the Experiments and Instructions for Runnning Them
================================================================================

Inmmutability 
-------------
The experiment in the paper is described in Sections 6.2.1 and 6.2.2.

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
