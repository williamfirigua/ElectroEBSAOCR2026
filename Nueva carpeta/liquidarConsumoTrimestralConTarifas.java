/**
 * NUCLEO de la busqueda/calculo de tarifa, SIN aplicar el ajuste al peso
 * (variables.ejecutarAjusteUnidades) todavia. Deja los valores RAW (con
 * decimales) en 'resultado', para que el llamador decida CUANDO redondear:
 *   - liquidarConsumoConTarifas (mensual) redondea de inmediato, porque
 *     solo hay un valor.
 *   - liquidarConsumoTrimestralConTarifas ACUMULA los 3 valores RAW de los
 *     3 meses primero, y redondea una sola vez al final -- asi no se
 *     pierden 1-2 pesos por redondear 3 veces por separado.
 *
 * IMPORTANTE: asume que la tabla de tarifas YA esta abierta (no abre ni
 * cierra el archivo) -- eso es responsabilidad de quien llama, para poder
 * abrir una sola vez en el caso trimestral en vez de 3.
 *
 * @param resultado arreglo de salida, tamano 2:
 *                    resultado[0] = valor de consumo RAW (sin redondear)
 *                    resultado[1] = subsidio/contribucion RAW (sin redondear)
 * @return 1 si encontro el consumo en la tabla; 0 si tuvo que usar el
 *         fallback (no encontrado)
 */
private int calcularValorTarifaRaw(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,
                                    String anio, String mes, String codtarifa,
                                    double[] resultado) {

    resultado[0] = 0;
    resultado[1] = 0;

    if (consumoSubsistencia < 0) consumoSubsistencia = 0;

    long consumoParaTabla;
    long consumoExcedente;
    if (consumo > consumoSubsistencia) {
        consumoParaTabla = Math.round(consumoSubsistencia);
        consumoExcedente = Math.round(consumo - consumoSubsistencia);
    } else {
        consumoParaTabla = Math.round(consumo);
        consumoExcedente = 0;
    }

    if (infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION().trim().equals("")) {
        infoClienteEntrada.settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION("0");
        infoClienteEntrada.settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION("0");
    }

    if (consumoSubsistencia == 0) {
        double valorConsumoRaw = valorReferenciaPorKwh * consumo;
        double subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION()) * valorConsumoRaw / 100;
        resultado[0] = valorConsumoRaw;
        resultado[1] = subsidioRaw;
        return 1;
    }

    String idBuscar = construirIdTarifa(anio, mes, codtarifa, consumoParaTabla);
    tablaTarifa.buscarbinario_TablaTarifas(idBuscar);

    if (tablaTarifa.encontro_TablaTarifas == 0) {
        Log.e("liquidacion", "No se encontro el consumo en TablaTarifas: " + idBuscar);

        double valorConsumoRaw = valorReferenciaPorKwh * consumo;

        String idMaximo = construirIdTarifa(anio, mes, codtarifa, 99999999);
        tablaTarifa.buscarbinario_TablaTarifas(idMaximo);

        double subsidioRaw;
        if (tablaTarifa.encontro_TablaTarifas > 0) {
            double diferencia = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim())
                               - parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());
            subsidioRaw = consumo * diferencia;
        } else {
            subsidioRaw = parsearDecimal(infoClienteEntrada.gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION())
                         * consumo * valorReferenciaPorKwh / 100;
        }

        resultado[0] = valorConsumoRaw;
        resultado[1] = subsidioRaw;
        return 0;
    }

    double valorNominal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORKWH().trim());
    double valorReal = parsearDecimal(tablaTarifa.gettablaTarifas_VLORRKWH().trim());

    double subsidioRaw = valorNominal - valorReal;
    double valorConsumoRaw = valorReal + (valorReferenciaPorKwh * consumoExcedente);

    resultado[0] = valorConsumoRaw;
    resultado[1] = subsidioRaw;
    return 1;
}


/**
 * Liquidacion MENSUAL (la que ya tenias funcionando) -- ahora implementada
 * sobre calcularValorTarifaRaw, aplicando el ajuste al peso UNA vez, igual
 * que antes. El comportamiento externo no cambia para quien ya la usa.
 */
private int liquidarConsumoConTarifas(double consumo, double consumoSubsistencia, double valorReferenciaPorKwh,
                                       String anio, String mes, String codtarifa,
                                       double resultadoNoUsado) {

    Pesos_energia_trimestra = 0;
    variables.pesosenergia = 0;
    int abriotarifas = 0;

    try {
        if (tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
            abriotarifas = 1;

            double[] raw = new double[2];
            int r = calcularValorTarifaRaw(consumo, consumoSubsistencia, valorReferenciaPorKwh,
                                            anio, mes, codtarifa, raw);

            variables.pesosenergia = variables.ejecutarAjusteUnidades(raw[0]);
            infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(
                    Integer.toString((int) variables.ejecutarAjusteUnidades(raw[1])));

            tablaTarifa.Cerrar_TablaTarifas();
            return r;
        } else {
            mensajeT("No se puede abrir tabla de tarifas", msgMedio);
        }
    } catch (Exception e) {
        mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
        String[] time = getTimeError().split("\\|");
        utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" +
                infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                serialPDA + "|" + "[MenuDeLiquidacion]liquidarConsumoConTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
        if (abriotarifas == 1) {
            tablaTarifa.Cerrar_TablaTarifas();
        }
        return -1;
    }

    return 1;
}


/**
 * Reparte un consumo trimestral total en 3 valores enteros por mes que
 * suman exactamente el total, repartiendo el residuo de la division entre
 * 3 de forma pareja.
 *   158 / 3 = 52 residuo 2  -> [53, 53, 52]
 *    91 / 3 = 30 residuo 1  -> [31, 30, 30]
 */
private int[] repartirConsumoTrimestral(int consumoTotal) {
    int base = consumoTotal / 3;
    int residuo = consumoTotal % 3;
    int[] meses = new int[3];
    for (int i = 0; i < 3; i++) {
        meses[i] = base + (i < residuo ? 1 : 0);
    }
    return meses;
}


/**
 * Liquidacion TRIMESTRAL sobre calcularValorTarifaRaw: abre la tabla de
 * tarifas UNA sola vez, hace las 3 busquedas mensuales, ACUMULA los 3
 * valores sin redondear, y aplica el ajuste al peso UNA sola vez sobre el
 * total -- para no perder 1-2 pesos redondeando 3 veces por separado.
 *
 * @param consumoTotal          consumo TOTAL del trimestre (entero, kWh)
 * @param consumoSubsistencia   limite de subsistencia MENSUAL (igual para los 3 meses)
 * @param valorReferenciaPorKwh tarifa de referencia por kWh (igual para los 3 meses)
 * @param anios                 arreglo de 3: ano de cada uno de los 3 meses del trimestre
 * @param meses                 arreglo de 3: mes de cada uno de los 3 meses del trimestre
 * @param codtarifa             codigo de tarifa del cliente
 * @return 1 si los 3 meses encontraron tarifa en tabla; 0 si alguno uso
 *         fallback; -1 si hubo una excepcion real
 */
private int liquidarConsumoTrimestralConTarifas(int consumoTotal, double consumoSubsistencia,
                                                 double valorReferenciaPorKwh,
                                                 String[] anios, String[] meses, String codtarifa) {

    int abriotarifas = 0;

    try {
        if (!tablaTarifa.abrir_TablaTarifas(tablaTarifa.getArchivo_TablaTarifas())) {
            mensajeT("No se puede abrir tabla de tarifas", msgMedio);
            return -1;
        }
        abriotarifas = 1;

        int[] consumoPorMes = repartirConsumoTrimestral(consumoTotal);
        double[] raw = new double[2];

        double totalRawValor = 0;
        double totalRawSubsidio = 0;
        int peorResultado = 1;

        for (int i = 0; i < 3; i++) {
            int r = calcularValorTarifaRaw(consumoPorMes[i], consumoSubsistencia, valorReferenciaPorKwh,
                                            anios[i], meses[i], codtarifa, raw);
            if (r < peorResultado) peorResultado = r;
            totalRawValor += raw[0];
            totalRawSubsidio += raw[1];
        }

        variables.pesosenergia = variables.ejecutarAjusteUnidades(totalRawValor);
        infoClienteSalida.settablaClienteSalida_CONTRIBUCIONENERGIA(
                Integer.toString((int) variables.ejecutarAjusteUnidades(totalRawSubsidio)));

        tablaTarifa.Cerrar_TablaTarifas();
        return peorResultado;

    } catch (Exception e) {
        mensajeT("Problemas procesando Tabla Tarifas", msgMedio);
        String[] time = getTimeError().split("\\|");
        utils.Log(logfile, infoClienteEntrada.gettablaEntradaClientes_Cuenta() + "|" + Ciclo.substring(6, 9) + "|" +
                infoClienteEntrada.gettablaEntradaClientes_mes() + "|" + infoClienteEntrada.gettablaEntradaClientes_anio() + "|" +
                serialPDA + "|" + "[MenuDeLiquidacion]liquidarConsumoTrimestralConTarifas()|" + e.getMessage() + "|" + time[0] + "|" + time[1] + "|");
        if (abriotarifas == 1) {
            tablaTarifa.Cerrar_TablaTarifas();
        }
        return -1;
    }
}
