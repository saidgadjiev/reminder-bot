<div class="container d-flex justify-content-center align-items-center flex-column flex-grow-1">
    <div id="open-link" class="w-100 justify-content-center">
        <div class="row justify-content-center">
            <div class="col-md-5">
                <h4 style="color: #1565C0">Reminder это</h4>
                <h3>Новый совершенный бот для создания напоминаний как себе так и друзьям. С этим ботом вы никогда
                    ничего не забудете!</h3>
                <p class="mt-5" style="text-align: justify">Бот работает по системе подписок. Первые <b>${trialPeriod} дней</b> бесплатно. Далее оплата по плану.</p>
                <dl class="row">
                    <dt class="col-sm-3">Цена</dt>
                    <dd class="col-sm-9">${price} рублей.</dd>

                    <dt class="col-sm-3">Период</dt>
                    <dd class="col-sm-9">${period}.</dd>
                </dl>
                <a id="link" class="btn btn-sm btn-primary btn-block" href="tg://resolve?domain=${botName}">Перейти в Reminder</a>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
    setTimeout(
        function () {
            window.location.href = 'tg://resolve?domain=${botName}';
        }, 2000
    );
</script>