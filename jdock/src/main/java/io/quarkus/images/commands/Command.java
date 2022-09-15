package io.quarkus.images.commands;

import io.quarkus.images.BuildContext;

public interface Command {

    String execute(BuildContext context);

}
