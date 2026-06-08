package br.edu.ifpe.sistemaeditais.model;

public enum ODS {
    ODS_1_ERRADICACAO_DA_POBREZA("1 - Erradicação da Pobreza"),
    ODS_2_FOME_ZERO("2 - Fome Zero e Agricultura Sustentável"),
    ODS_3_SAUDE_BEM_ESTAR("3 - Saúde e Bem-Estar"),
    ODS_4_EDUCACAO_QUALIDADE("4 - Educação de Qualidade"),
    ODS_5_IGUALDADE_GENERO("5 - Igualdade de Gênero"),
    ODS_6_AGUA_SANEAMENTO("6 - Água Potável e Saneamento"),
    ODS_7_ENERGIA_ACESSIVEL("7 - Energia Limpa e Acessível"),
    ODS_8_TRABALHO_CRESCIMENTO("8 - Trabalho Decente e Crescimento Econômico"),
    ODS_9_INDUSTRIA_INOVACAO("9 - Indústria, Inovação e Infraestrutura"),
    ODS_10_REDUCAO_DESIGUALDADES("10 - Redução das Desigualdades"),
    ODS_11_CIDADES_COMUNIDADES("11 - Cidades e Comunidades Sustentáveis"),
    ODS_12_CONSUMO_PRODUCAO("12 - Consumo e Produção Responsáveis"),
    ODS_13_ACAO_CLIMA("13 - Ação contra a Mudança Global do Clima"),
    ODS_14_VIDA_AGUA("14 - Vida na Água"),
    ODS_15_VIDA_TERRESTRE("15 - Vida Terrestre"),
    ODS_16_PAZ_JUSTICA("16 - Paz, Justiça e Instituições Eficazes"),
    ODS_17_PARCERIAS_MEIOS("17 - Parcerias e Meios de Implementação");

    private final String descricao;

    ODS(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}