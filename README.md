# Verification_lab1

Программа получает файл .с как аргумент программы и строит SSA-модель переданного С-кода. Поддерживает следующие структуры: if/else, switch, for, while, do/while и операторы break, continue.

Для парсинга исходного кода используется Antrl4, грамматика языка С для парсера взята из https://github.com/antlr/grammars-v4/blob/master/c/C.g4 и доработана для удобства использования.

