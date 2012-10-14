package com.googlecode.compilo;

public interface ResourceHandler {
    boolean matches(String name);
    Resource handle(Resource resource);

    class methods {
        public static Outputs decorate(final Outputs outputs, final Iterable<ResourceHandler> resourceHandlers) {
            return new Outputs() {
                @Override
                public void put(Resource resource) {
                    for (ResourceHandler resourceHandler : resourceHandlers) {
                        if(resourceHandler.matches(resource.name())) {
                            resource = resourceHandler.handle(resource);
                        }
                    }
                    outputs.put(resource);
                }
            };
        }
    }
}
