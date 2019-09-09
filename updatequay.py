## Update description by parsing Cekit yaml and extract io.k8s.description
## and updating repo via quay.io api.
## Require application token from an organization that have write access.

import requests
import yaml

from argparse import ArgumentParser

parser = ArgumentParser()
parser.add_argument('files', metavar="Files", type=str, nargs="+", help="File to extract metadata from.")
parser.add_argument('--token', metavar="Token", type=str, required=True, help="quay.io token that has write access.")
args = parser.parse_args()


for f in args.files:

    with open(f, 'r') as stream:
        try:
            data = yaml.safe_load(stream)
        except yaml.YAMLError as exc:
            print("Error parsing: " + f)

    if data:
        name = data['name']
        if name.startswith("quay.io/"):
            name = name[len("quay.io/"):]

            repoapiurl = 'https://quay.io/api/v1/repository/' + name
            
            reallabels = data['labels']
            labels = {}
            for l in reallabels:
                labels[l["name"]] = l["value"]

            description = labels["io.k8s.description"] + "\n\n\n[Source](https://github.com/quarkusio/quarkus-images)"

            print("Setting '{0}' description to '{1}'".format(name, description))
            r = requests.put(repoapiurl,
                             headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + args.token},
                             json={"description":description})

            print(r.text)
        else:
            print("Skipping " + f + " as not referencing quay.io")
