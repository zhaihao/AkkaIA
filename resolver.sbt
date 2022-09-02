Global / resolvers   += "GitHub Package Registry" at "https://maven.pkg.github.com/zhaihao/repos"
Global / credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", "zhaihao", sys.env("GITHUB_TOKEN"))
