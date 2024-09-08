# Test file to check that inferred metadata is functioning correctly

## Introduction: what metadata is expected?

Cryogen expects a [number of items of metadata](http://cryogenweb.org/docs/writing-posts.html#post-contents) to be declared as front-matter in a file. The default format of this frontmatter is [Extensible Data Notation](https://github.com/edn-format/edn), but as of verion 0.2.7, [YAML](https://yaml.org/) and [JSON](https://www.json.org/json-en.html) may be used as alternatives. YAML is the metadata format used by [Zettlr](https://docs.zettlr.com/en/core/yaml-frontmatter/) and by [PanDoc](https://pandoc.org/MANUAL.html#extension-yaml_metadata_block).

## How is metadata inferred?

Metadata is inferred by functions in the namespace [`cryogen-core.infer-meta`](https://github.com/cryogen-project/cryogen-core/blob/master/src/cryogen_core/infer_meta.clj). Specifically, the following items are inferred:

### Title

Title is inferred from the first main headline in the document, if present; otherwise, from the file name of the document.

### Description

Description is inferred from the first normal paragraph of the document, if present; otherwise, from the title.

### Date

Date is inferred from the basename of the file, if there is a date part present (it is a convention of Cryogen file names that they should start with a date part), otherwise, from the creation date of the file.

### Author

Author is inferred by querying the operating system for the real name associated with the user who is running the current execution. The mechanism I'm using is a [library of my own](https://github.com/simon-brooke/real-name), which should be reliable on UN*X like systems but may fail on Windows based systems with non-English locales.

### Tags

'Tags', in Cryogen, are used to create subject oriented sub-indexes of the site, dividing posts by topic. Alternative words for this might be 'subjects', or 'keywords'. Tags are inferred from lines in the file whose first token is the strongly emphasised string 'Tags:', if any. The remainders of these lines, after the first token, will be treated as comma separated values.