package com.example.garred.doramatv;

public class VkWorker {
    /* Класс занимается обменом данных с vk api


    * Посылает запрос и получает JSON ответ. Берет из ответа ссылки на видео с различными качествами
    * Авторизация, генерация токена
    * https://oauth.vk.com/authorize?client_id=6863889&scope=video,offline&redirect_uri=http://api.vk.com/blank.html&display=page&response_type=token
    *
    * Возвращаемое значение
    * http://api.vk.com/blank.html#access_token=d053e5de82599c59b61a8a138cfe732d462a245623f8807ee3a4bf5a9dad3e22f1179377b0499001932f0&expires_in=0&user_id=14942038
    *
    * Сохраняем access_token
    *
    * Для доступа к видео парсим страницу до объекта: div class="chapter-link"
    * Внутри берем <span data-sid="248871" и <a href="/list/person/sub_unit_zoloto" class="person-link">Sub-Unit Zoloto</a>
    *
    * Полученный data-sid подставляем в http://grass.tragus.ru/internal/videoCode/#data-sid
    *
    * Загружаем страницу.
    * <iframe src="https://vk.com/video_ext.php?oid=-66384560&amp;id=456239144&amp;hash=f159cbb66cd617e3" allowfullscreen=""></iframe>
    *
    * Забираем oid и id , формируем oid_id, формируем https://api.vk.com/method/video.get?videos=#oid_id&access_token=#token&v=#version
    *
    * Получаем json ответ. Забираем ссылки на файл из files.
    *
    * */
}
