# Como Compilar o FeatherAPI para EXE

Este documento descreve os passos necessários para gerar um executável (.exe) portátil para Windows a partir do código-fonte Java.

## 1. Gerar o Fat JAR
O primeiro passo é empacotar o projeto Java com todas as suas dependências em um único arquivo `.jar` (conhecido como Fat JAR ou Uber JAR).

```bash
mvn clean package
```
*   **Resultado:** Um arquivo chamado `feather-api-1.0.jar` será criado na pasta `target/`.
*   **Nota:** O arquivo `pom.xml` já está configurado com o `maven-shade-plugin` para incluir as dependências (Gson, FlatLaf, etc.).

## 2. Gerar o Executável (.exe) com `jpackage`
O `jpackage` é uma ferramenta do JDK (disponível do Java 14 em diante) que cria uma imagem da aplicação incluindo um Java Runtime (JRE) embutido. Isso permite que o programa rode em computadores que não possuem Java instalado.

### Preparação
Para evitar que arquivos desnecessários do Maven entrem no pacote final, criamos uma pasta temporária apenas com o JAR necessário:

```powershell
mkdir jpackage-temp
copy target\feather-api-1.0.jar jpackage-temp\
```

### Comando de Compilação
Execute o comando abaixo (ajustando o caminho do JDK se necessário):

```powershell
& "C:\Program Files\Java\jdk-21.0.10\bin\jpackage.exe" `
  --type app-image `
  --name FeatherAPI `
  --input jpackage-temp `
  --main-jar feather-api-1.0.jar `
  --main-class Main `
  --icon src/main/resources/feather.ico `
  --dest dist
```

**Explicação dos parâmetros:**
*   `--type app-image`: Cria uma pasta com o executável e dependências (em vez de um instalador).
*   `--name FeatherAPI`: Nome do executável final.
*   `--input jpackage-temp`: Pasta onde o `jpackage` vai buscar o JAR.
*   `--main-jar`: O nome do JAR principal dentro da pasta de input.
*   `--main-class`: A classe que contém o método `public static void main`.
*   `--icon`: Caminho para o arquivo de ícone (.ico).
*   `--dest dist`: Pasta de destino onde o resultado será salvo.

## 3. Estrutura do Resultado
Após a execução, a pasta `dist/FeatherAPI/` conterá:
*   `FeatherAPI.exe`: O inicializador do programa.
*   `app/`: Contém o seu JAR e configurações.
*   `runtime/`: Uma versão reduzida do Java (JRE) necessária para rodar o app.

**IMPORTANTE:** Para distribuir seu programa, você deve enviar a pasta **FeatherAPI inteira** (geralmente compactada em um .zip). O arquivo `.exe` sozinho não funcionará sem as pastas `app` e `runtime`.

## 4. Limpeza
Após gerar o executável, você pode remover a pasta temporária:
```powershell
Remove-Item -Recurse -Force jpackage-temp
```
