# TRAVELING SALES PROBLEM

Relazione di Elton Stafa

## Introduzione

L' obiettivo del progetto è quello di confrontare tra loro 3 diversi algoritmi per il problema del tsp:
- Nearest Neighbor
- Algoritmo di Held-Karp
- Algoritmo 2-approssimato

Inoltre per ogni algoritmo verrà utilizzato lo stesso dataset che contiene 13 grafi con un numero di nodi che varia tra 13 a 1000.

### Descrizione degli algoritmi

Durante il corso sono stati presentati i seguenti algoritmi in pseudocodifica:

### Nearest Neighbor

1. Inizializzazione: si parte con il cammino composto dal solo vertice 0
2. Selezione: sia (v0,..., vk) il cammino corrente. Trova il vertice vk+1 non ancora inserito
   nel circuito a distanza minima da vk;
3. Inserimento: inserisci vk+1 subito dopo vk nel cammino;
4. ripeti da (2) finché non hai inserito tutti i vertici nel cammino;
5. chiudi il circuito inserendo il vertice iniziale 0 alla fine del cammino (0,..., vn).

### Held Karp

      function HK-VISIT(v,S)
         if S = {v} then // Caso base: la soluzione è il peso dell’arco {v,0}
            return w[v,0]
         else if d[v,S] 6= NULL then // Distanza già calcolata, ritorna il valore memorizzato
            return d[v,S]
         else // Caso ricorsivo: trova il minimo tra tutti i sottocammini
            mindist = ∞
            minprec = NULL
            for all u ∈ S \ {v} do
               dist = HK-VISIT(u,S \ {v})
               if dist +w[u, v] < mindist then
                  mindist = dist +w[u, v]
                  minprec = u
               end if
            end for
            d[v,S] = mindist
            π[v,S] = minprec
            return mindist
         end if
      end function

### Algoritmo 2-approssimato

      function APPROX-2(G, c)
         V = {v1, v2, ..., vn}
         R = v1
         T* = MST(G, c, R) // ottieni un mst con Prim etc.
         H' = DFS(T*, R) // visita preorder
         w  = COMPUTE(H') // calcola il peso
         return w


## Scelte implementative

Per lo sviluppo del progetto ho scelto di utilizzare **Scala** (v2.13.4) con sbt (v1.5.0). Il motivo è di semplice preferenza personale, Scala come linguaggio funzionale permette di lavorare con oggetti immutabili e funzioni "high-order" che facilitano lo sviluppo, inoltre, non c'erano vincoli sul tipo di linguaggio da utilizzare.


### Implementazione di Nearest Neighbor

```scala
  @tailrec
  private def doNearestNeighbor(graph: Graph, s: Int, tsp: Graph): Graph = { // O(nm)
    if(graph.edges.isEmpty) tsp
    else {
      val verticesInTsp = tsp.vertices.map(v => (v, v)).toMap
      val nearest = graph.edges
        // find nearest vertices not already inserted
         .filter(e =>
          (e.u == s && !verticesInTsp.contains(e.v))
            || (e.v == s) && !verticesInTsp.contains(e.u)
        )
        // take the smallest Vk+1 in term of cost
        .minByOption(_.w)
      if(nearest.isDefined) {
        val newS = if(s == nearest.get.v) nearest.get.u else nearest.get.v
        val newTsp = Graph(newS +: tsp.vertices, nearest.get +: tsp.edges)
        val newGraph = Graph(graph.vertices, graph.edges.filter(_ != nearest.get))
        doNearestNeighbor(newGraph, newS, newTsp)
      } else {
        tsp
      }
    }
  }
```

L' algoritmo è molto semplice e ricorsivo sul numero di lati. 
Il caso base (nessun lato) ritorna direttamente il tsp, altrimenti, si recuperano tutti i nodi vicini a quello corrente 
e si sceglie quello con distanza minima.

In termini di complessità asintotica non è stato particolarmente ottimizzato con strutture dati a supporto (a parte semplici liste)
dato che lo scopo per questo laboratorio è quello di ragionare sulla approssimazione dei risultati.

L' algoritmo è comunque in grado di processare tutti i grafi in pochi secondi e rispecchia la 
pseudocodifica presentata durante il corso.


### Implementazione di Held Karp

```scala
private def doHeldKarp(graph: Graph, v: Int, S: Seq[Int], d: Map[(Int, Seq[Int]), Int]): Int = {
    S match {
      case s::Nil if s == v =>
        graph.edges.find(e => e.u == v && e.v == 1).map(_.w)
        .getOrElse(graph.edges.find(e => e.u == 1 && e.v == v).map(_.w).get)
      case _ =>
        if(d.contains((v, S))) d(v, S)
        else {
          val sMinusV = S.filter(_ != v)
          var minDist = Int.MaxValue

          sMinusV.foreach(u => {
            val dist = doHeldKarp(graph, u, sMinusV, d)
            val maybeMinDist =
              dist + graph.edges.find(e => e.u == u && e.v == v).map(_.w)
                .getOrElse(graph.edges.find(e => e.u == v && e.v == u).map(_.w).get)

            if(maybeMinDist < minDist) {
              minDist = maybeMinDist
            }

            // TIMEOUT
            if((System.nanoTime() - t0) > 180.seconds.toNanos)
              throw TimerException(minDist)

          })
          d.update((v, S), minDist)
          minDist
        }
    }
  }
```
L' algoritmo Held Karp ha una complessità esponenziale e utilizza quindi un timeout di 3 minuti per restituire un risultato che 
potrebbe essere parziale. 

Esso si basa sull' utilizzo di una mappa **d** che come chiave ha una tupla composta da un nodo v e un insieme di nodi S [(v, S)].
Ogni entry della mappa conserva la somma delle distanze dal nodo di partenza fino a **v** che passa per i nodi dell' insieme S.

La cosa interessante è che il numero di chiamate ricorsive è limitato superiormente dal numero di entry in **d** (notare il primo if che ritorna la distanza).
Esse sono infatti al massimo **n · 2^n** perchè v è un nodo del grafo (ce ne sono n) ed S è un sottoinsieme di nodi.
Quindi la complessità totale è **O(n^2 · 2^n)**. 

### Implementazione dell' algoritmo 2-approssimato

```scala
  def approx2(graph: Graph): Int = {
    val mst = MST.unionFindKruskal(graph)
    val adjList = buildAdjList(mst.edges)
    val path = dfs(adjList, 1, Nil)
    val pathPath = path.prepended(1).toVector
   
    var w = 0
    for(i <- path.indices) {
      // must be one 
      val edge = graph.edges
        .find(e => (e.u == pathPath(i) && e.v == pathPath(i+1)) || (e.u == pathPath(i+1) && e.v == pathPath(i)))

      w = w + edge.head.w
    }
    w
  }
```

L' implementazione dell' algoritmo anche in questo caso è molto semplice, si parte calcolando un minimum spanning tree 
per il grafo in input, successivamente si effettua una visita **dfs** sull' albero ottenuto e in questo modo si ottiene
un cammino di nodi e per ognuno di essi si calcola la distanza con il successivo.

Per calcolare l' mst ho deciso di utilizzare Kruskal con union find, in quanto più rapido e facile da integrare in questo 
progetto. 

La complessità asintotica dell' algoritmo è data dal calcolo dell' mst e quindi **O(m logn)**.

Anche in questo ultimo caso l' algoritmo è conforme alla pseudo codifica.

## Originalità introdotte nella implementazione

Per quanto riguarda l' implementazione, alcune note degne di menzione possono essere:

- utilizzo della notation @tailrec ove possibile per una ottimizzazione da parte del compilatore della ricorsione.
- scala come linguaggio di programmazione funzionale invece dei più comuni python o java.

## Grafici delle tempistiche

![Tabella delle soluzioni](tab.png?raw=true "Tabella delle soluzioni")

## Conclusioni

- i dati sono stati riportati sulla tabella e sono stati calcolati gli errori sulla soluzione ottima per ogni grafo.

- gli algoritmi si comportano diversamente per la varie istanze, per esempio held-karp a causa della sua complessità elevata 
  restituisce delle soluzioni **parziali** per quasi tutti i grafi tranne uno: burma14, nella quale è stata trovata la soluzione ottima 
  entro i 3 minuti imposti come timeout. 
  Sarebbe stato possibile trovare le soluzioni ottime di ulysses16 e ulysses22 ottimizzando 
  un po' di più l' algoritmo (ma non era lo scopo del laboratorio) oppure aumentando di qualche secondo il time limit.
  
  Interessante notare come l' euristica scelta (nearest neighbor) ritorni una soluzione di "buona" approssimazione anche se per 
  definizione non abbiamo nessuna garanzia che ciò avvenga se non appunto delle ipotesi su come sia costruito il dataset.
  
  L' algoritmo 2-approssimato invece ritorna anch' esso delle soluzioni approssimate al più di un fattore uguale 2 e questa
  oltre a essere una garanzia è anche confermata dai risultati ottenuti.
  
- per quanto concerne l' efficienza, l' algoritmo 2 approssimato ha mediamente i tempi migliori (agevolato dalla costruzione di mst con union find),
  seguito da nearest neighbor ed infine Held Karp. 
  Quest' ultimo però, idealmente è quello che con sufficiente tempo e memoria è in grado di fornire la soluzione esatta,
  come dimostrato per il grafo burma14.
  
