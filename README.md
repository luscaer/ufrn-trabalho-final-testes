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

<img width="1450" height="351" alt="image" src="https://github.com/user-attachments/assets/6ffc440e-21ef-4ca8-bf72-df810cfb2032" />

## Cobertura de arestas

### Gerar relatório de cobertura
<pre> mvn jacoco:report </pre>

<img width="1919" height="652" alt="image" src="https://github.com/user-attachments/assets/f4661929-4730-46bc-acbb-e60acf2c396e" />

### O relatório de cobertura do jacoco ficará disponível em:
target/site/jacoco/index.html

## Cobertura de mutantes calcularCustoTotal()

### Gerar relatório de testes mutantes com pitest
<pre> mvn pitest:mutationCoverage </pre>

<img width="1919" height="652" alt="image" src="https://github.com/user-attachments/assets/114bc593-9c82-4cb5-affd-b61cb27c5820" />

Na primeira execução do relatório de mutantes, apenas um havia sobrevivido

<img width="1450" height="411" alt="image" src="https://github.com/user-attachments/assets/1036c819-278e-431b-abab-dcdbf9b11631" />

Ele foi morto adicionando um caso de teste com valor de carrinho = 0.00

## Cobertura de mutantes finalizarCompra()

### Gerar relatório de testes mutantes com pitest
<pre> mvn pitest:mutationCoverage </pre>

<img width="1919" height="465" alt="image" src="https://github.com/user-attachments/assets/63db0020-09d7-40ad-a2fc-192d4d7a74af" />

Na primeira execução do relatório de mutantes, 3 mutantes sobreviveram

<img width="1919" height="652" alt="image" src="https://github.com/user-attachments/assets/f9dd6220-8378-4006-af62-14b1d9b894a3" />
<img width="1345" height="172" alt="image" src="https://github.com/user-attachments/assets/bafddf19-cdf8-4d8b-a059-e7962b30646c" />

Os dois primeiros foram mortos adicionando uma verificação dos ids e quantidades recebidas.
<img width="857" height="320" alt="image" src="https://github.com/user-attachments/assets/550cc0d8-8784-4b3c-bca5-8b7470c91222" />
<img width="857" height="122" alt="image" src="https://github.com/user-attachments/assets/ac70c556-16ab-4a61-8b2e-cc0282992200" />

O terceiro foi morto criando uma verificação se o chamado de cancelamento continua sendo true quando não acontece baixa
<img width="462" height="111" alt="image" src="https://github.com/user-attachments/assets/52d14331-1f6b-437d-8b04-e654c8f48b8f" />
<img width="771" height="45" alt="image" src="https://github.com/user-attachments/assets/38dba449-32a9-49c7-af30-8748938d53e7" />

### O relatório de cobertura dos testes mutantes ficará disponível em:
target/pit-reports/index.html

## Instruções de Execução Pré-requisitos

- Java 17
- Maven 3.8+
- JUnit 5
- AssertJ
- Jacoco (para cobertura)

### Executar os testes
<pre> mvn clean test </pre>
