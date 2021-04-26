# MINIMUM SPANNING TREE

Relazione di Stafa Elton

## Introduzione

L'obiettivo del progetto è quello di confrontare tra loro tre diversi algoritmi per il problema del minimum spanning tree.
Gli algoritmi sono:

- Algoritmo di Prim implementato con Heap
- Algoritmo di Kruskal nella sua implementazione "naive"
- Algoritmo di Kruskal implementato con Union-Find

Inoltre, per ogni algoritmo verrà usato lo stesso dataset contenente 68 grafi di dimensione compresa tra 10 e 100000 vertici.

### Descrizione degli algoritmi

Durante il corso sono stati presentati i seguenti algoritmi in pseudocodifica:

#### Heap implementato con Heap di complessità O(m log(n))

    heapPrim(G, s)
        foreach v in V
            key[v] = +inf
            pi[v]  = null

        key[s] = 0
        Q      = V   // priority queue

        while Q.length != 0
            u = extractMin(Q)
            foreach v adjacent to u
                if v in Q AND w(u,v) < key[v]
                    pi[v]  = v
                    key[v] = w(u,v)

        return "implicit" A = {(v, pi[v]) : v in V \ S \ Q}

#### Kruskal Naive di complessità O(mn)

    kruskal(G)
        A = Nil
        sortEdgesByCost(G) // increasing order with mergesort

        foreach e in E
            if (A ++ [e]).isAcyclic
                A = A ++ [e]

        return A

#### Kruskal con Union Find

    kruskalUF(G)
        A = Nil
        U = init(V) // union find struct
        sortEdgesByCost(G)

        foreach e in E
            if find(U,v) != find(U, w)
                A = A ++ [(v,w)]
                union(U, v, w)  // update struct
    return A

## Scelte implementative

Per lo sviluppo del progetto ho scelto di utilizzare **Scala** (v2.13.4) con sbt (v1.5.0). Il motivo è di semplice preferenza personale, Scala come linguaggio funzionale permette di lavorare con oggetti immutabili e funzioni "high-order" che facilitano lo sviluppo, inoltre, non c'erano vincoli sul tipo di linguaggio da utilizzare.

### Implementazione di Prim con Heap

Per l'implementazione di Prim con Heap in Scala ho per prima cosa definito una classe che rappresentasse uno Heap. La classe si appoggia su due strutture dati (**Vector** e **Map**) e memorizza all'interno di se stessa uno stato che serve per garantire l'immutabilità.

La classe VectorHeap è quindi così definita:

```scala
class VectorHeap[K: Ordering, V] private (
    vector: Vector[Entry[K, V]],
    map: Map[V, Int]
) extends Heap[K, V] {

    private case class State(
        vector: Vector[Entry[K, V]],
        map: Map[V, Int]
    )

    // ...

}
```

La struttura "vector" contiene una collezione di **Entry** chiave-valore dove la chiave rappresenta la priorità del nodo (esattamente quella che viene inizializzata a +inf nell'algoritmo in pseudocodifica citato sopra) mentre il valore è il "nome" del nodo (che nel nostro caso corrisponde al vertice).

La struttura "map" invece è utilizzato da supporto a vector e memorizza come chiave il del nodo/vertice mentre come valore la posizione/indice del nodo all'interno di vector.

Questo permette di accedere a qualsiasi nodo del grafo in tempo costante, infatti conoscendo l'indice del vertice memorizzato in "vector" è possibile ottenere i "padri" o i "figli" semplicemente moltiplicando o dividendo il valore dell'indice per 2.

Ciò risulta molto utile all'interno delle funzioni ricorsive che una struttura Heap deve esporre:

- **extractMin** è la funzione ricorsiva che ritorna il nodo con chiave di priorità più "piccola" ed ha una complessita di O(log n) come richiesto.
- **decreaseKey** è la funzione ricorsiva che aggiorna il valore della chiave di un nodo nel vettore. Questa operazione sarebbe impossibile senza la struttura map che memorizza l'indice di ogni nodo nel vettore e rende la complessità della funzione pari a O(log n).

Entrambe le funzioni, come tutto il codice del progetto, è commentato quindi è possibile verificare facilmente che la complessità delle funzioni sia quella dichiarata.

La classe VectorHeap utilizza al suo interno due funzioni **bubbleUp** e **bubbleDown** che hanno lo scopo di "riparare" lo Heap successivamente alle operazioni appena sopra elencate.

```scala
  def heapPrim(graph: Graph, s: Int = 1): Graph = {
    val entries =
        graph.vertices
        .map(v =>
            if (v == s) Entry(0, v) else Entry(Int.MaxValue, v)
        )
    val heap    = VectorHeap(entries)
    recHeapPrim(heap, graph, Graph(graph.vertices, Nil)
  }
```

```scala
  @tailrec
  private def recHeapPrim(heap: Heap[Int, Int], graph: Graph, mst: Graph): Graph =
    if (heap.isEmpty) mst
    else {
      val (maybeU, heapAfterExtraction) = heap.extractMin
      val edgesAdjacentToU  = getAdjacencyList(graph, maybeU.get.value)
      val (mstAfterUpdate, heapAfterUpdate) = updateHeap(
          edgesAdjacentToU,
          heapAfterExtraction,
          mst
      )
      recHeapPrim(heapAfterUpdate, graph, mstAfterUpdate)
    }
```

```scala
@tailrec
  private def updateHeap(
      edgesAdjacentToU: (Int, Seq[Edge]),
      heap: Heap[Int, Int],
      mst: Graph
  ): (Graph, Heap[Int, Int]) =
    if (edgesAdjacentToU._2.isEmpty) (mst, heap) // trivial
    else {
      // update the key value
      if (heap.exist(edgesAdjacentToU._2.head.v).isDefined &&
      edgesAdjacentToU._2.head.w < heap.key(edgesAdjacentToU._2.head.v)
      ) {
        val updatedMst =
          Graph(mst.vertices, edgesAdjacentToU._2.head +: mst.edges)

        updateHeap(
          (edgesAdjacentToU._1, edgesAdjacentToU._2.tail),
          heap.decreaseKey(edgesAdjacentToU._2.head.v, edgesAdjacentToU._2.head.w),
          updatedMst
        )
      } else
        updateHeap((edgesAdjacentToU._1, edgesAdjacentToU._2.tail), heap, mst)
    }
```

La funzione heapPrim() inizializza la struttura heap e il grafo utilizzato come buffer per costruire l'mst.

Inoltre chiama la funzione recHeapPrim() che esegue per ricorsione sul numero di elementi dell'heap che vengono estratti uno dopo l'altro dalla funzione extractMin().

A questo punto si prelevano gli archi adiacenti al vertice estratto dallo heap e si passa alla funzione updateHeap() che per ricorsione sul numero di archi adiacenti aggiorna la chiave di priorità del vertice nella struttura heap (tramite decreaseKey).

Infine se l'arco sotto esame rispetta le condizioni (ricordo w(u,v) < key(v)), esso viene aggiunto al mst risultante. Da notare che l'arco viene aggiunto sempre in testa alla lista degli archi, infatti un inserimento in coda avrebbe complessità O(n) invece che costante e porterebbe l'algoritmo ad una complessità finale di O(mn log(n)) invece della sua naturale O(m log(n)).

### Implementazione di Kruskal "naive"

L'algoritmo di Kruskal, nella sua versione più semplice, è stato scritto senza l'utilizzo di classi ad-hoc.

Esso infatti è molto semplice e fa uso semplicemente del grafo che in questo progetto è rappresentato da una classe **Graph** che contiene una lista di vertici, una di archi e una mappa rappresentante la lista di adiacenza.

```scala
  @tailrec
  private def recNaiveKruskal(mst: Graph, graph: Graph): Graph =
    if (graph.edges.isEmpty) mst // O(k)
    else {
      if (isCyclic(mst, graph.edges.head)) { // O(n)
        // if the graph is cyclic then ignore the selected edge
        recNaiveKruskal(mst, Graph(graph.vertices, graph.edges.tail, graph.adjacencyList))
      } else {
        recNaiveKruskal(
          buildGraph(graph.edges.head +: mst.edges), // O(n)
          Graph(graph.vertices, graph.edges.tail, graph.adjacencyList)
        )
      }
    }

  def naiveKruskal(graph: Graph): Graph = {
    val graphAfterSort = sortedGraph(graph) // TimSort O(n * log(n))
    val bufferMst      = Graph(Nil, Nil)
    val mst            = recNaiveKruskal(bufferMst, graphAfterSort)
    mst
  }
```

L'algoritmo è quasi autoesplicativo, quello che fa è ordinare la lista di archi in modo crescente di peso (Scala usa timsort come funzione di ordinamento).

Successivamente viene chiamata la funzione che effettua la ricorsione sul numero di archi e quest'ultima verifica che l'mst "sommato" con l'arco in esame non formi un ciclo, se ciò non avviene viene aggiornato il grafo mst da ritornare.

La ciclicità del grafo viene verificata chiamando la funzione DFS (depth first search) che in tempo lineare effettua il compito.

```scala
  def dfs(
      adjacencyList: AdjacencyList,
      u: Int,
      visited: Map[Int, Int]): Map[Int, Int] = {

    if(visited.contains(u)) visited // O(K)
    else if(!adjacencyList.contains(u)) visited // O(k)
    else adjacencyList(u)
        .foldLeft(visited + (u -> u))((updatedVisited, e) => dfs(adjacencyList, e.v, updatedVisited))

  }
```

Kruskal "naive" in questa implementazione ha complessità O(nm).

### Implementazione Kruskal Union Find

In questo algoritmo utilizziamo una struttura di supporto per abbattere il costo della versione "naive". La struttura utilizzata è la **Union-Find** che ho deciso di implementare con un vettore (scala.Vector come HeapPrim).

```scala
case class Node(parent: Option[Int], treeSize: Int)

class UnionFind private (nodes: Vector[Node])
```

il vettore consente di indicizzare i vertici in tempo costante mentre la classe Node memorizza il padre del vertice di indice "i" e la grandezza del sotto-albero.

Una struttura Union Find deve esporre 2 funzioni:

- **union** è la funzione ricorsiva che aggiorna la struttura dati tramite l'unione di 2 alberi (per la precisione quelli dei vertici v e w citati nella pseudocodifica). Tale unione avviene considerando la dimensione degli alberi in quando è fondamentale mantenere l'albero complessivo il più ampio possibile come spiegato a lezione, in modo tale da sfruttare la complessità logaritmica quando si ricerca un nodo in un albero.

- **find** è la funzione ricorsiva che risale fino alla radice di un albero dato un vertice. Ciò è fondamentale per determinare quali alberi unire nella union.

La complessità di find è proporzionale alla depth dell'oggetto che sto cercando, quindi nel caso peggiore O(n-1).
Per union la complessità è invece solo logaritmica.

```scala

  @tailrec
  private def recUnionFindKruskal(unionFindStruct: UnionFind, bufferMst: Graph, edges: Seq[Edge]): Graph =
    if (edges.isEmpty) bufferMst
    else {
      val e = edges.head
      if (unionFindStruct.find(e.u, e.v)) {
        recUnionFindKruskal(unionFindStruct, bufferMst, edges.tail)
      } else {
        recUnionFindKruskal(
          unionFindStruct.union(e.u, e.v),
          Graph(bufferMst.vertices,  e +: bufferMst.edges),
          edges.tail
        )
      }
    }

  def unionFindKruskal(graph: Graph): Graph = {
    val graphAfterSort  = sortedGraph(graph)
    val bufferMst       = Graph(graph.vertices, Nil)
    val unionFindStruct = UnionFind.create(numVertices(graph) + 1)
    recUnionFindKruskal(unionFindStruct, bufferMst, graphAfterSort.edges)
  }
```

Per prima cosa inizializzo la struttura union find, successivamente ordino gli archi e chiamo la funzione recUnionFindKruskal che effettua la ricorsione sul numero di archi.

Infine chiamo la funzione find su i 2 vertici dell'arco e se questi appartengono a due alberi diversi aggiorno la struttura dati chiamando la funzione union.

Ad ogni passo della ricorsione il grafo mst viene aggiornato.
La complessità finale dell'algoritmo è O(m log(n)).

## Originalità introdotte nella implementazione

Per quanto riguarda l'implementazione, alcune note degne di menzione possono essere:

- il mantenimento dell'immutabilità come caratteristica in ogni algoritmo per evitare qualsiasi side effect.
- gestione dello stato in heap prim tramite una combinazione di un vettore e mappa per l'indicizzazione.
- utilizzo della notation @tailrec ove possivile per una ottimizzazione da parte del compilatore della ricorsione.
- scala come linguaggio di programmazione funzionale invece dei più comuni python o java.

## Grafici delle tempistiche
