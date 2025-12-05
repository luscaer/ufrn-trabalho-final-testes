# Projeto: JoséCarlos-LucasEmanuel

Funcionalidade: Finalização de Compra

Este projeto implementa e testa a funcionalidade de cálculo do custo total da 
compra (CompraService.calcularCustoTotal) de forma isolada, utilizando Java 17, JUnit 5 e AssertJ.

Autores
- José Carlos da Silva
- Lucas Emanuel Ribeiro Costa

## Descrição da Funcionalidade

A funcionalidade de finalização de compra calcula o preço total considerando:

- Subtotal dos produtos.
- Desconto por valor total da compra (>500 ou >1000).
- Cálculo de frete baseado em faixas de peso.
- Taxa adicional para produtos frágeis.
- Arredondamento final para duas casas decimais (Half-up).

## Partições de domínio e valores limites do domínio
<img width="1411" height="353" alt="image" src="https://github.com/user-attachments/assets/60e38a8e-d406-440a-95b7-4a781be182f0" />


## Instruções de Execução Pré-requisitos

- Java 17
- Maven 3.8+
- JUnit 5
- AssertJ
- Jacoco (para cobertura)

### Executar os testes
<pre> mvn clean test </pre>

### Gerar relatório de cobertura
<pre>mvn jacoco:report</pre>

### O relatório ficará disponível em:
target/site/jacoco/index.html

