# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

## [0.5.0] - 2025-09-26
### Changed
- step input to be the `:temp` hash (#7)

## [0.4.0] - 2025-09-06
### Added
- `defstep` and `|=|` macros to `workflow.step` (#2)

## [0.3.0] - 2025-09-05
### Added
- access to results of previous steps (#3)
- _destination_ keys
- _labels_
- delivers result of last step under the `:out` key
### Changed
- `workflow.core/make` expects _labels_
- input parameters are available under the `:in` key
### Removed
- automatic generation of labels

## [0.2.0] - 2025-08-28
### Added
- step composition
- short-circuiting
- wilful failure

## 0.1.0 - 2025-08-20
### Added
- project scaffold

[Unreleased]: https://github.com/eureton/workflow/compare/0.5.0...HEAD
[0.5.0]: https://github.com/eureton/workflow/compare/0.4.0...0.5.0
[0.4.0]: https://github.com/eureton/workflow/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/eureton/workflow/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/eureton/workflow/compare/0.1.0...0.2.0
