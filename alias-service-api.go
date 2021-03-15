package main

import (
	"fmt"
	"net/http"
)

func main() {
	http.HandleFunc("/", HelloServer)
	http.ListenAndServe(":7080", nil)
}

func HelloServer(w http.ResponseWriter, r *http.Request) {
      fmt.Fprintf(w, "%s-alias", r.URL.Path[1:])
}