export GO111MODULE=on

GO ?= go
EXECUTABLE ?= hallinto

.PHONY: all
all: build

.PHONY: help
help:
	@echo "Make Routines:"
	@echo " - \"\"                               equivalent to \"build\""
	@echo " - build                            build everything"
	@echo " - watch                            watch everything and continuously rebuild"

.PHONY: backend
backend: generate $(EXECUTABLE)

.PHONY: generate
generate:
	@echo "Running go generate..."
	@CC= GOOS= GOARCH= $(GO) generate -mod=vendor $(GO_PACKAGES)

$(EXECUTABLE): $(GO_SOURCES)
	CGO_CFLAGS="$(CGO_CFLAGS)" $(GO) build -mod=vendor $(GOFLAGS) $(EXTRA_GOFLAGS) -tags '$(TAGS)' -ldflags '-s -w $(LDFLAGS)' -o $@

watch:
	@hash air > /dev/null 2>&1; if [ $$? -ne 0 ]; then \
		GO111MODULE=off $(GO) get -u github.com/cosmtrek/air; \
	fi
	air -c .air.toml