rootProject.name = "Pipeline"

include(
    ":core",
    ":json",
    ":mongodb",
    ":redis",
    ":sql",
    ":mysql",
    ":h2",
    ":sqlite"
)
include("gson")
