# TRAVELING SALES PROBLEM

Relazione di Elton Stafa

## Introduzione

L'obiettivo del progetto è quello di confrontare tra loro 3 diversi algoritmi per il problema del tsp:
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

