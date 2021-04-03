package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"time"

	"github.com/gorilla/mux"
	"github.com/jmoiron/sqlx"
	_ "github.com/lib/pq"
)

var psqlconn string

func init() {
	host := flag.String("host", "localhost", "PostgreSQL host")
	port := flag.Int("port", 5432, "PostgreSQL port")
	user := flag.String("user", "postgres", "PostgreSQL user")
	password := flag.String("password", "", "PostgreSQL password")
	dbname := flag.String("dbname", "hallinto", "PostgreSQL database")

	flag.Parse()

	psqlconn = fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable", *host, *port, *user, *password, *dbname)
}

func main() {
	db, err := sqlx.Open("postgres", psqlconn)
	CheckError(err)
	defer db.Close()

	err = db.Ping()
	CheckError(err)

	r := mux.NewRouter()
	r.HandleFunc("/", HomeHandler)

	StartServer(r)
}

func CheckError(err error) {
	if err != nil {
		panic(err)
	}
}

func HomeHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintln(w, "Hello world")
}

func StartServer(r *mux.Router) {
	srv := &http.Server{
		Addr:         "0.0.0.0:8080",
		WriteTimeout: time.Second * 15,
		ReadTimeout:  time.Second * 15,
		IdleTimeout:  time.Second * 60,
		Handler:      r,
	}

	go func() {
		log.Println("Starting server on port 8080")
		if err := srv.ListenAndServe(); err != nil {
			log.Println(err)
		}
	}()

	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)

	<-c

	ctx, cancel := context.WithTimeout(context.Background(), time.Second*15)
	defer cancel()
	srv.Shutdown(ctx)
	log.Println("shutting down")
	os.Exit(0)
}
